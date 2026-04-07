package com.gepardec.mega.hexagon.worktime.domain.model;

public record WorkTimeEntry(
        WorkTimeEmployee employee,
        WorkTimeProject project,
        double billableHours,
        double nonBillableHours,
        double employeeMonthTotalHours
) {
}
