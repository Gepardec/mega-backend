package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;

public record MonthEndStatusOverviewItem(
        MonthEndTaskId taskId,
        MonthEndTaskType type,
        MonthEndTaskStatus status,
        ProjectId projectId,
        UserId subjectEmployeeId,
        UserId completedBy
) {
}
