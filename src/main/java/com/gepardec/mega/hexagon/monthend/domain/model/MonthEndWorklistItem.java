package com.gepardec.mega.hexagon.monthend.domain.model;

public record MonthEndWorklistItem(
        MonthEndTaskId taskId,
        MonthEndTaskType type,
        MonthEndProject project,
        MonthEndEmployee subjectEmployee
) {
}
