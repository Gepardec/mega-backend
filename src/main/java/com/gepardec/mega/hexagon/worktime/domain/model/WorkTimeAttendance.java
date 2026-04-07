package com.gepardec.mega.hexagon.worktime.domain.model;

public record WorkTimeAttendance(
        String employeeZepId,
        Integer projectZepId,
        double billableHours,
        double nonBillableHours
) {

    public double totalHours() {
        return billableHours + nonBillableHours;
    }
}
