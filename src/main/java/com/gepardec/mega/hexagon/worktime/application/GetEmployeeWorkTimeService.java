package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeUserNotFoundException;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeValidationException;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEmployee;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEntry;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeProject;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import com.gepardec.mega.hexagon.worktime.domain.port.inbound.GetEmployeeWorkTimeUseCase;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeZepPort;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetEmployeeWorkTimeService implements GetEmployeeWorkTimeUseCase {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final WorkTimeZepPort workTimeZepPort;

    @Inject
    public GetEmployeeWorkTimeService(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            WorkTimeZepPort workTimeZepPort
    ) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.workTimeZepPort = workTimeZepPort;
    }

    @Override
    public WorkTimeReport getWorkTime(UserId employeeId, YearMonth month) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new WorkTimeUserNotFoundException("user not found: " + employeeId.value()));

        return workTimeZepPort.fetchAttendancesForEmployee(requireZepUsername(employee), month)
                .map(attendances -> toReport(employee, month, attendances))
                .await().indefinitely();
    }

    private WorkTimeReport toReport(User employee, YearMonth month, List<WorkTimeAttendance> attendances) {
        if (attendances.isEmpty()) {
            return new WorkTimeReport(month, List.of());
        }

        double employeeMonthTotalHours = totalHours(attendances);
        WorkTimeEmployee employeeRef = new WorkTimeEmployee(employee.id(), fullName(employee.name()));

        List<WorkTimeEntry> entries = attendances.stream()
                .collect(Collectors.groupingBy(WorkTimeAttendance::projectZepId))
                .entrySet().stream()
                .map(entry -> toEntry(employeeRef, entry.getKey(), entry.getValue(), employeeMonthTotalHours))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing((WorkTimeEntry entry) -> entry.project().name()))
                .toList();

        return new WorkTimeReport(month, entries);
    }

    private Optional<WorkTimeEntry> toEntry(
            WorkTimeEmployee employeeRef,
            Integer zepProjectId,
            List<WorkTimeAttendance> attendances,
            double employeeMonthTotalHours
    ) {
        return projectRepository.findByZepId(zepProjectId)
                .map(project -> new WorkTimeEntry(
                        employeeRef,
                        new WorkTimeProject(project.id(), project.name()),
                        sumBillableHours(attendances),
                        sumNonBillableHours(attendances),
                        employeeMonthTotalHours
                ))
                .or(() -> {
                    Log.warnf("Skipping worktime entry for unknown project zepId=%s", zepProjectId);
                    return Optional.empty();
                });
    }

    private String requireZepUsername(User user) {
        if (user.zepUsername() == null || user.zepUsername().value().isBlank()) {
            throw new WorkTimeValidationException("zep username missing for user: " + user.id().value());
        }
        return user.zepUsername().value();
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
