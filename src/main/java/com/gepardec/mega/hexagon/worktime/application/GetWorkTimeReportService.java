package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.shared.domain.model.Capacity;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetWorkTimeReportUseCase;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeProjectSnapshotPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeUserSnapshotPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeZepPort;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeUserNotFoundException;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeValidationException;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
// No transaction: ZEP calls fan out via Mutiny, and a propagated JTA context races with commit.
@Transactional(Transactional.TxType.NEVER)
public class GetWorkTimeReportService implements GetWorkTimeReportUseCase {

    private final WorkTimeProjectSnapshotPort workTimeProjectSnapshotPort;
    private final WorkTimeUserSnapshotPort workTimeUserSnapshotPort;
    private final WorkTimeZepPort workTimeZepPort;

    @Inject
    public GetWorkTimeReportService(
            WorkTimeProjectSnapshotPort workTimeProjectSnapshotPort,
            WorkTimeUserSnapshotPort workTimeUserSnapshotPort,
            WorkTimeZepPort workTimeZepPort
    ) {
        this.workTimeProjectSnapshotPort = workTimeProjectSnapshotPort;
        this.workTimeUserSnapshotPort = workTimeUserSnapshotPort;
        this.workTimeZepPort = workTimeZepPort;
    }

    @Override
    public WorkTimeReport getWorkTime(UserId actorId, YearMonth month, Capacity capacity) {
        Optional<WorkTimeScope> scope = switch (capacity) {
            case EMPLOYEE -> Optional.of(employeeScope(actorId, month));
            case CONTROLLING -> controllingScope(actorId, month);
        };
        return scope.map(workTimeScope -> buildReport(workTimeScope, month))
                .orElseGet(() -> WorkTimeReport.empty(month));
    }

    private WorkTimeScope employeeScope(UserId actorId, YearMonth month) {
        UserRef employee = workTimeUserSnapshotPort.findById(actorId, month)
                .orElseThrow(() -> new WorkTimeUserNotFoundException("user not found: " + actorId.value()));
        String zepUsername = requireZepUsername(employee);

        return new WorkTimeScope(
                Map.of(zepUsername, employee),
                zepId -> workTimeProjectSnapshotPort.findByZepId(zepId, month),
                false
        );
    }

    private Optional<WorkTimeScope> controllingScope(UserId actorId, YearMonth month) {
        List<ProjectRef> leadProjects = workTimeProjectSnapshotPort.findAllByLead(actorId, month);
        if (leadProjects.isEmpty()) {
            return Optional.empty();
        }

        Map<Integer, ProjectRef> projectsByZepId = leadProjects.stream()
                .collect(Collectors.toMap(ProjectRef::zepId, Function.identity()));

        Set<String> employeeZepUsernames = fetchEmployeeZepIds(projectsByZepId.keySet(), month)
                .await().indefinitely();
        if (employeeZepUsernames.isEmpty()) {
            return Optional.empty();
        }

        Map<String, UserRef> usersByZepUsername = workTimeUserSnapshotPort.findByZepUsernames(employeeZepUsernames.stream()
                        .map(ZepUsername::of)
                        .collect(Collectors.toSet()), month)
                .stream()
                .filter(user -> user.zepUsername() != null)
                .collect(Collectors.toMap(user -> user.zepUsername().value(), Function.identity()));

        // Bookings on non-lead projects are expected here.
        return Optional.of(new WorkTimeScope(usersByZepUsername, zepId -> Optional.ofNullable(projectsByZepId.get(zepId)), true));
    }

    private WorkTimeReport buildReport(WorkTimeScope scope, YearMonth month) {
        Map<String, List<WorkTimeAttendance>> attendancesByZepUsername =
                fetchAttendancesByEmployee(scope.employeesByZepUsername().keySet(), month)
                        .await().indefinitely();

        Map<UserRef, List<WorkTimeAttendance>> attendancesByEmployee = attendancesByZepUsername.entrySet().stream()
                .collect(Collectors.toMap(entry -> scope.employeesByZepUsername().get(entry.getKey()), Map.Entry::getValue));

        return WorkTimeReport.assemble(month, attendancesByEmployee, resolveProjects(attendancesByEmployee, scope));
    }

    private Map<Integer, ProjectRef> resolveProjects(Map<UserRef, List<WorkTimeAttendance>> attendancesByEmployee, WorkTimeScope scope) {
        Map<Integer, ProjectRef> resolved = new HashMap<>();
        Set<Integer> unresolved = new TreeSet<>();
        attendancesByEmployee.values().stream()
                .flatMap(List::stream)
                .map(WorkTimeAttendance::projectZepId)
                .distinct()
                .forEach(zepId -> scope.projects().resolve(zepId)
                        .ifPresentOrElse(project -> resolved.put(zepId, project), () -> unresolved.add(zepId)));
        logUnresolvedProjects(unresolved, scope);
        return resolved;
    }

    private void logUnresolvedProjects(Set<Integer> unresolvedZepIds, WorkTimeScope scope) {
        if (unresolvedZepIds.isEmpty()) {
            return;
        }
        if (scope.unresolvedProjectsExpected()) {
            Log.debugf("Skipping worktime entries for projects outside the scope, zepIds=%s", unresolvedZepIds);
        } else {
            Log.warnf("Skipping worktime entries for unknown projects, zepIds=%s", unresolvedZepIds);
        }
    }

    private Uni<Set<String>> fetchEmployeeZepIds(Collection<Integer> projectZepIds, YearMonth month) {
        return Multi.createFrom().iterable(projectZepIds)
                .onItem().transformToUniAndMerge(projectZepId -> workTimeZepPort.fetchProjectMembershipForMonth(projectZepId, month))
                .collect().asList()
                .map(projectMemberships -> projectMemberships.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toSet()));
    }

    private Uni<Map<String, List<WorkTimeAttendance>>> fetchAttendancesByEmployee(Collection<String> employeeZepIds, YearMonth month) {
        return Multi.createFrom().iterable(employeeZepIds)
                .onItem().transformToUniAndMerge(employeeZepId -> workTimeZepPort.fetchAttendancesForEmployee(employeeZepId, month)
                        .map(attendances -> Map.entry(employeeZepId, attendances)))
                .collect().asList()
                .map(entries -> entries.stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private String requireZepUsername(UserRef user) {
        if (user.zepUsername() == null || user.zepUsername().value().isBlank()) {
            throw new WorkTimeValidationException("zep username missing for user: " + user.id().value());
        }
        return user.zepUsername().value();
    }
}
