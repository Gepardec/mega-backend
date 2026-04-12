package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEmployee;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEntry;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeProject;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeProjectSnapshot;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeUserSnapshot;
import com.gepardec.mega.hexagon.worktime.domain.port.inbound.GetProjectLeadWorkTimeUseCase;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeProjectSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeUserSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeZepPort;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetProjectLeadWorkTimeService implements GetProjectLeadWorkTimeUseCase {

    private final WorkTimeProjectSnapshotPort workTimeProjectSnapshotPort;
    private final WorkTimeUserSnapshotPort workTimeUserSnapshotPort;
    private final WorkTimeZepPort workTimeZepPort;

    @Inject
    public GetProjectLeadWorkTimeService(
            WorkTimeProjectSnapshotPort workTimeProjectSnapshotPort,
            WorkTimeUserSnapshotPort workTimeUserSnapshotPort,
            WorkTimeZepPort workTimeZepPort
    ) {
        this.workTimeProjectSnapshotPort = workTimeProjectSnapshotPort;
        this.workTimeUserSnapshotPort = workTimeUserSnapshotPort;
        this.workTimeZepPort = workTimeZepPort;
    }

    @Override
    public WorkTimeReport getWorkTime(UserId callerId, YearMonth month) {
        List<WorkTimeProjectSnapshot> leadProjects = workTimeProjectSnapshotPort.findAllByLead(callerId);
        if (leadProjects.isEmpty()) {
            return new WorkTimeReport(month, List.of());
        }

        Map<Integer, WorkTimeProjectSnapshot> projectsByZepId = leadProjects.stream()
                .collect(Collectors.toMap(WorkTimeProjectSnapshot::zepId, Function.identity()));

        Set<String> employeeZepUsernames = fetchEmployeeZepIds(projectsByZepId.keySet(), month)
                .await().indefinitely();
        if (employeeZepUsernames.isEmpty()) {
            return new WorkTimeReport(month, List.of());
        }

        Map<String, List<WorkTimeAttendance>> attendancesByEmployee = fetchAttendancesByEmployee(employeeZepUsernames, month)
                .await().indefinitely();

        Map<String, WorkTimeUserSnapshot> usersByZepUsername = workTimeUserSnapshotPort.findByZepUsernames(employeeZepUsernames.stream()
                        .map(ZepUsername::of)
                        .collect(Collectors.toSet()))
                .stream()
                .filter(user -> user.zepUsername() != null)
                .collect(Collectors.toMap(WorkTimeUserSnapshot::zepUsername, Function.identity()));

        List<WorkTimeEntry> entries = attendancesByEmployee.entrySet().stream()
                .flatMap(entry -> toEntries(entry.getKey(), entry.getValue(), usersByZepUsername, projectsByZepId).stream())
                .sorted(Comparator.comparing((WorkTimeEntry entry) -> entry.employee().name())
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
            Map<String, WorkTimeUserSnapshot> usersByZepUsername,
            Map<Integer, WorkTimeProjectSnapshot> projectsByZepId
    ) {
        WorkTimeUserSnapshot user = usersByZepUsername.get(employeeZepId);
        if (user == null) {
            Log.warnf("Skipping worktime entries for unknown employee zepId=%s", employeeZepId);
            return List.of();
        }

        double employeeMonthTotalHours = totalHours(attendances);
        WorkTimeEmployee employeeRef = new WorkTimeEmployee(user.id(), user.fullName());

        return attendances.stream()
                .filter(attendance -> projectsByZepId.containsKey(attendance.projectZepId()))
                .collect(Collectors.groupingBy(WorkTimeAttendance::projectZepId))
                .entrySet().stream()
                .map(entry -> toEntry(employeeRef, employeeMonthTotalHours, entry.getKey(), entry.getValue(), projectsByZepId))
                .toList();
    }

    private WorkTimeEntry toEntry(
            WorkTimeEmployee employeeRef,
            double employeeMonthTotalHours,
            Integer zepProjectId,
            List<WorkTimeAttendance> attendances,
            Map<Integer, WorkTimeProjectSnapshot> projectsByZepId
    ) {
        WorkTimeProjectSnapshot project = projectsByZepId.get(zepProjectId);
        return new WorkTimeEntry(
                employeeRef,
                new WorkTimeProject(project.id(), project.name()),
                sumBillableHours(attendances),
                sumNonBillableHours(attendances),
                employeeMonthTotalHours
        );
    }

    private double totalHours(List<WorkTimeAttendance> attendances) {
        return attendances.stream()
                .mapToDouble(WorkTimeAttendance::totalHours)
                .sum();
    }

    private double sumBillableHours(List<WorkTimeAttendance> attendances) {
        return attendances.stream()
                .mapToDouble(WorkTimeAttendance::billableHours)
                .sum();
    }

    private double sumNonBillableHours(List<WorkTimeAttendance> attendances) {
        return attendances.stream()
                .mapToDouble(WorkTimeAttendance::nonBillableHours)
                .sum();
    }

}
