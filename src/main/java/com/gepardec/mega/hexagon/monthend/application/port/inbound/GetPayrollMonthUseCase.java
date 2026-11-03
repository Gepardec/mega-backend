package com.gepardec.mega.hexagon.monthend.application.port.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.Capacity;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;

public interface GetPayrollMonthUseCase {

    YearMonth getPayrollMonth(UserId actorId, Capacity capacity);
}
