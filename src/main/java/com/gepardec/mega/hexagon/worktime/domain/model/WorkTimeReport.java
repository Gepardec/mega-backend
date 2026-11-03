package com.gepardec.mega.hexagon.worktime.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record WorkTimeReport(YearMonth payrollMonth, List<WorkTimeEntry> entries) {

    public static WorkTimeReport empty(YearMonth payrollMonth) {
        return new WorkTimeReport(payrollMonth, List.of());
    }

    /**
     * Builds one entry per employee and resolved project. Bookings on projects missing from
     * {@code projectsByZepId} produce no entry but still count towards the employee's month total.
     */
    public static WorkTimeReport assemble(
            YearMonth payrollMonth,
            Map<UserRef, List<WorkTimeAttendance>> attendancesByEmployee,
            Map<Integer, ProjectRef> projectsByZepId
    ) {
        List<WorkTimeEntry> entries = attendancesByEmployee.entrySet().stream()
                .flatMap(entry -> entriesForEmployee(entry.getKey(), entry.getValue(), projectsByZepId).stream())
                .toList();
        return new WorkTimeReport(payrollMonth, entries);
    }

    private static List<WorkTimeEntry> entriesForEmployee(
            UserRef employee,
            List<WorkTimeAttendance> attendances,
            Map<Integer, ProjectRef> projectsByZepId
    ) {
        double employeeMonthTotalHours = attendances.stream()
                .mapToDouble(WorkTimeAttendance::totalHours)
                .sum();

        return attendances.stream()
                .collect(Collectors.groupingBy(WorkTimeAttendance::projectZepId))
                .entrySet().stream()
                .flatMap(entry -> Optional.ofNullable(projectsByZepId.get(entry.getKey()))
                        .map(project -> WorkTimeEntry.of(employee, project, entry.getValue(), employeeMonthTotalHours))
                        .stream())
                .toList();
    }
}
