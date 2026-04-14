package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;

import java.util.Objects;

public record MonthEndStatusOverviewItem(
        MonthEndTaskId taskId,
        MonthEndTaskType type,
        MonthEndTaskStatus status,
        ProjectRef project,
        UserRef subjectEmployee,
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
