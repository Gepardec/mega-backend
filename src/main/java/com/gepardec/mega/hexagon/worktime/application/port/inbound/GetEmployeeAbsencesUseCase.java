package com.gepardec.mega.hexagon.worktime.application.port.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.domain.model.Absence;

import java.time.YearMonth;
import java.util.List;

public interface GetEmployeeAbsencesUseCase {

    List<Absence> getAbsences(UserId employeeId, YearMonth month);
}
