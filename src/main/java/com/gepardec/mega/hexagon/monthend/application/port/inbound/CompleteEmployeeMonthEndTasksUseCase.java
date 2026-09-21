package com.gepardec.mega.hexagon.monthend.application.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;
import java.util.List;

public interface CompleteEmployeeMonthEndTasksUseCase {

    List<MonthEndTask> complete(YearMonth month, UserId actorId);
}
