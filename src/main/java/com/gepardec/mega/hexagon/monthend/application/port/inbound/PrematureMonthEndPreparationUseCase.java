package com.gepardec.mega.hexagon.monthend.application.port.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;

public interface PrematureMonthEndPreparationUseCase {

    void prepare(YearMonth month, UserId actorId, String clarificationText);
}
