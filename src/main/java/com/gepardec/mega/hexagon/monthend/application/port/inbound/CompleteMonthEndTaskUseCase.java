package com.gepardec.mega.hexagon.monthend.application.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

public interface CompleteMonthEndTaskUseCase {

    MonthEndTask complete(MonthEndTaskId taskId, UserId actorId);
}
