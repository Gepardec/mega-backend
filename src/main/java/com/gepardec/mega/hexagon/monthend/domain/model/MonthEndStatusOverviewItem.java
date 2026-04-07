package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.util.Objects;

public record MonthEndStatusOverviewItem(
        MonthEndTaskId taskId,
        MonthEndTaskType type,
        MonthEndTaskStatus status,
        MonthEndProject project,
        MonthEndEmployee subjectEmployee,
        boolean canComplete,
        UserId completedBy
) {

    public MonthEndStatusOverviewItem {
        Objects.requireNonNull(taskId, "taskId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(project, "project must not be null");
    }
}
