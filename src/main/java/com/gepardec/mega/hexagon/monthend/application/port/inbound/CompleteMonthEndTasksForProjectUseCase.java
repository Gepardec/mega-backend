package com.gepardec.mega.hexagon.monthend.application.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;
import java.util.List;

public interface CompleteMonthEndTasksForProjectUseCase {
    List<MonthEndTask> complete (YearMonth month, ProjectId projectId, MonthEndTaskType type, UserId actorId);
}

