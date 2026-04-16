package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetProjectLeadWorkTimeUseCase;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEntry;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeProjectSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeUserSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeZepPort;
import com.gepardec.mega.hexagon.worktime.domain.services.WorkTimeReportAssembler;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.YearMonth;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class GetProjectLeadWorkTimeService implements GetProjectLeadWorkTimeUseCase {

    private final WorkTimeProjectSnapshotPort workTimeProjectSnapshotPort;
    private final WorkTimeUserSnapshotPort workTimeUserSnapshotPort;
    private final WorkTimeZepPort workTimeZepPort;
    private final WorkTimeReportAssembler workTimeReportAssembler;

    @Inject
    public GetProjectLeadWorkTimeService(
            WorkTimeProjectSnapshotPort workTimeProjectSnapshotPort,
            WorkTimeUserSnapshotPort workTimeUserSnapshotPort,
            WorkTimeZepPort workTimeZepPort,
            WorkTimeReportAssembler workTimeReportAssembler
    ) {
        this.workTimeProjectSnapshotPort = workTimeProjectSnapshotPort;
        this.workTimeUserSnapshotPort = workTimeUserSnapshotPort;
        this.workTimeZepPort = workTimeZepPort;
        this.workTimeReportAssembler = workTimeReportAssembler;
    }

    @Override
    public WorkTimeReport getWorkTime(UserId callerId, YearMonth month) {
        List<ProjectRef> leadProjects = workTimeProjectSnapshotPort.findAllByLead(callerId, month);
        if (leadProjects.isEmpty()) {
            return new WorkTimeReport(month, List.of());
        }

        Map<Integer, ProjectRef> projectsByZepId = leadProjects.stream()
                .collect(Collectors.toMap(ProjectRef::zepId, Function.identity()));

        Set<String> employeeZepUsernames = fetchEmployeeZepIds(projectsByZepId.keySet(), month)
                .await().indefinitely();
        if (employeeZepUsernames.isEmpty()) {
            return new WorkTimeReport(month, List.of());
        }

        Map<String, UserRef> usersByZepUsername = workTimeUserSnapshotPort.findByZepUsernames(employeeZepUsernames.stream()
                        .map(ZepUsername::of)
                        .collect(Collectors.toSet()), month)
                .stream()
                .filter(user -> user.zepUsername() != null)
                .collect(Collectors.toMap(user -> user.zepUsername().value(), Function.identity()));

        Map<String, List<WorkTimeAttendance>> attendancesByEmployee = fetchAttendancesByEmployee(usersByZepUsername.keySet(), month)
                .await().indefinitely();

        List<WorkTimeEntry> entries = attendancesByEmployee.entrySet().stream()
                .flatMap(entry -> toEntries(entry.getKey(), entry.getValue(), usersByZepUsername, projectsByZepId).stream())
                .sorted(Comparator.comparing((WorkTimeEntry entry) -> entry.employee().fullName().displayName())
                        .thenComparing(entry -> entry.project().name()))
                .toList();

        return new WorkTimeReport(month, entries);
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

    private List<WorkTimeEntry> toEntries(
            String employeeZepId,
            List<WorkTimeAttendance> attendances,
            Map<String, UserRef> usersByZepUsername,
            Map<Integer, ProjectRef> projectsByZepId
    ) {
        UserRef user = usersByZepUsername.get(employeeZepId);
        if (user == null) {
            Log.warnf("Skipping worktime entries for unknown employee zepId=%s", employeeZepId);
            return List.of();
        }

        double employeeMonthTotalHours = workTimeReportAssembler.totalHours(attendances);

        return attendances.stream()
                .filter(attendance -> projectsByZepId.containsKey(attendance.projectZepId()))
                .collect(Collectors.groupingBy(WorkTimeAttendance::projectZepId))
                .entrySet().stream()
                .map(entry -> toEntry(user, employeeMonthTotalHours, entry.getKey(), entry.getValue(), projectsByZepId))
                .toList();
    }

    private WorkTimeEntry toEntry(
            UserRef employeeRef,
            double employeeMonthTotalHours,
            Integer zepProjectId,
            List<WorkTimeAttendance> attendances,
            Map<Integer, ProjectRef> projectsByZepId
    ) {
        ProjectRef project = projectsByZepId.get(zepProjectId);
        return workTimeReportAssembler.buildEntry(employeeRef, project, attendances, employeeMonthTotalHours);
    }
}
