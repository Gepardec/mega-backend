package com.gepardec.mega.hexagon.monthend.application.port.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface MonthEndEmployeeAbsencePort {

    List<LocalDate> findQualifyingAbsentDays(UserId employeeId, YearMonth month);
}
