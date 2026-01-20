package com.gepardec.mega.rest.model;

public record AttendancesDto(
        double totalWorkingTimeHours,
        double overtimeHours,
        double billableTimeHours,
        double billablePercentage
) {
}
