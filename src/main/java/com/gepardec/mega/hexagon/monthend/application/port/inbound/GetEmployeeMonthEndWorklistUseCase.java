package com.gepardec.mega.hexagon.monthend.application.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;

public interface GetEmployeeMonthEndWorklistUseCase {

    MonthEndWorklist getWorklist(UserId employeeId, YearMonth month);
}
