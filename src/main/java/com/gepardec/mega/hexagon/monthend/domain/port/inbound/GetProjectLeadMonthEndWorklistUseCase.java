package com.gepardec.mega.hexagon.monthend.domain.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.time.YearMonth;

public interface GetProjectLeadMonthEndWorklistUseCase {

    MonthEndWorklist getWorklist(UserId projectLeadId, YearMonth month);
}
