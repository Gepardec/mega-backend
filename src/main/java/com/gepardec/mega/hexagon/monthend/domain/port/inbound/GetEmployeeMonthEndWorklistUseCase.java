package com.gepardec.mega.hexagon.monthend.domain.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.time.YearMonth;

public interface GetEmployeeMonthEndWorklistUseCase {

    MonthEndWorklist getWorklist(UserId employeeId, YearMonth month);
}
