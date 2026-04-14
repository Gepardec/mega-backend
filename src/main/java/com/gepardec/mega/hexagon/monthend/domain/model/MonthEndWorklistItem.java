package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;

public record MonthEndWorklistItem(
        MonthEndTaskId taskId,
        MonthEndTaskType type,
        ProjectRef project,
        UserRef subjectEmployee
) {
}
