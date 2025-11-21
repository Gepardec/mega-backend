package com.gepardec.mega.domain.model;

public record Attendances(
        double totalWorkingTimeHours,
        double overtimeHours,
        double billableTimeHours,
        double billablePercentage
) {
}
