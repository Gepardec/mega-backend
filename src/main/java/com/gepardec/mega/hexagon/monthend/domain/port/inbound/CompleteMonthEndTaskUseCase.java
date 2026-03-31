package com.gepardec.mega.hexagon.monthend.domain.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;

public interface CompleteMonthEndTaskUseCase {

    MonthEndTask complete(MonthEndTaskId taskId, UserId actorId);
}
