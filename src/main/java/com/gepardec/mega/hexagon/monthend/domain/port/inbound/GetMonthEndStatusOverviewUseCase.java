package com.gepardec.mega.hexagon.monthend.domain.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.time.YearMonth;

public interface GetMonthEndStatusOverviewUseCase {

    MonthEndStatusOverview getOverview(UserId actorId, YearMonth month);
}
