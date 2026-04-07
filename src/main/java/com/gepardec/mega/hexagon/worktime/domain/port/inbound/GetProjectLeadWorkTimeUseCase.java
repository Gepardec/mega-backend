package com.gepardec.mega.hexagon.worktime.domain.port.inbound;

import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;

import java.time.YearMonth;

public interface GetProjectLeadWorkTimeUseCase {

    WorkTimeReport getWorkTime(UserId callerId, YearMonth month);
}
