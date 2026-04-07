package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEmployee;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEntry;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeProject;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import com.gepardec.mega.hexagon.worktime.domain.port.inbound.GetProjectLeadWorkTimeUseCase;
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

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WorkTimeZepPort workTimeZepPort;

    @Inject
    public GetProjectLeadWorkTimeService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            WorkTimeZepPort workTimeZepPort
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.workTimeZepPort = workTimeZepPort;
    }

    @Override
    public WorkTimeReport getWorkTime(UserId callerId, YearMonth month) {
        List<Project> leadProjects = projectRepository.findAllByLead(callerId);
        if (leadProjects.isEmpty()) {
            return new WorkTimeReport(month, List.of());
        }

        Map<Integer, Project> projectsByZepId = leadProjects.stream()
                .collect(Collectors.toMap(Project::getZepId, Function.identity()));

        Set<String> employeeZepIds = fetchEmployeeZepIds(projectsByZepId.keySet(), month)
                .await().indefinitely();
        if (employeeZepIds.isEmpty()) {
            return new WorkTimeReport(month, List.of());
        }

        Map<String, List<WorkTimeAttendance>> attendancesByEmployee = fetchAttendancesByEmployee(employeeZepIds, month)
                .await().indefinitely();

        Map<String, User> usersByZepUsername = userRepository.findByZepUsernames(employeeZepIds).stream()
                .collect(Collectors.toMap(user -> user.getZepProfile().username(), Function.identity()));

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
            Map<String, User> usersByZepUsername,
            Map<Integer, Project> projectsByZepId
    ) {
        User user = usersByZepUsername.get(employeeZepId);
        if (user == null) {
            Log.warnf("Skipping worktime entries for unknown employee zepId=%s", employeeZepId);
            return List.of();
        }

        double employeeMonthTotalHours = totalHours(attendances);
        WorkTimeEmployee employeeRef = new WorkTimeEmployee(user.getId(), fullName(user.getName()));

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
            Map<Integer, Project> projectsByZepId
    ) {
        Project project = projectsByZepId.get(zepProjectId);
        return new WorkTimeEntry(
                employeeRef,
                new WorkTimeProject(project.getId(), project.getName()),
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

    private String fullName(FullName fullName) {
        return (fullName.firstname() + " " + fullName.lastname()).trim();
    }
}
