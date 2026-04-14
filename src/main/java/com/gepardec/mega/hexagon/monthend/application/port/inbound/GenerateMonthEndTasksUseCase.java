package com.gepardec.mega.hexagon.monthend.application.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;

import java.time.YearMonth;

public interface GenerateMonthEndTasksUseCase {

    MonthEndTaskGenerationResult generate(YearMonth month);
}
