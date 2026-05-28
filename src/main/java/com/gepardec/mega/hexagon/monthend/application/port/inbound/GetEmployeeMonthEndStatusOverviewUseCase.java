package com.gepardec.mega.hexagon.monthend.application.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;

public interface GetEmployeeMonthEndStatusOverviewUseCase {

    MonthEndStatusOverview getOverview(UserId employeeId, YearMonth month);
}
