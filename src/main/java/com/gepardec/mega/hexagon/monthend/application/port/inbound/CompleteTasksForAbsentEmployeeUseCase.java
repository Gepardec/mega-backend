package com.gepardec.mega.hexagon.monthend.application.port.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;
import java.util.Optional;

public interface CompleteTasksForAbsentEmployeeUseCase {

    Optional<AbsentEmployeeAutoCompletion> complete(UserId employeeId, YearMonth month);
}
