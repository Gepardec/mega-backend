package com.gepardec.mega.hexagon.worktime.domain.services;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEntry;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class WorkTimeReportAssembler {

    public double totalHours(List<WorkTimeAttendance> attendances) {
        return attendances.stream()
                .mapToDouble(WorkTimeAttendance::totalHours)
                .sum();
    }

    public WorkTimeEntry buildEntry(
            UserRef employee,
            ProjectRef project,
            List<WorkTimeAttendance> projectAttendances,
            double employeeMonthTotalHours
    ) {
        return new WorkTimeEntry(
                employee,
                project,
                sumBillableHours(projectAttendances),
                sumNonBillableHours(projectAttendances),
                employeeMonthTotalHours
        );
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
