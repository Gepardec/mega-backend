package com.gepardec.mega.hexagon.worktime.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;

import java.util.List;

public record WorkTimeEntry(
        UserRef employee,
        ProjectRef project,
        double billableHours,
        double nonBillableHours,
        double employeeMonthTotalHours
) {

    public static WorkTimeEntry of(
            UserRef employee,
            ProjectRef project,
            List<WorkTimeAttendance> projectAttendances,
            double employeeMonthTotalHours
    ) {
        return new WorkTimeEntry(
                employee,
                project,
                projectAttendances.stream().mapToDouble(WorkTimeAttendance::billableHours).sum(),
                projectAttendances.stream().mapToDouble(WorkTimeAttendance::nonBillableHours).sum(),
                employeeMonthTotalHours
        );
    }
}
