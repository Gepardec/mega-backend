package com.gepardec.mega.hexagon.worktime.application.port.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.time.YearMonth;
import java.util.List;

public interface GetEmployeeWarningsUseCase {
    List<WorkTimeWarning> getWarnings(UserId employeeId, YearMonth month);
}
