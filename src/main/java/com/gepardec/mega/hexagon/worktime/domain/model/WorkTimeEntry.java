package com.gepardec.mega.hexagon.worktime.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;

public record WorkTimeEntry(
        UserRef employee,
        ProjectRef project,
        double billableHours,
        double nonBillableHours,
        double employeeMonthTotalHours
) {
}
