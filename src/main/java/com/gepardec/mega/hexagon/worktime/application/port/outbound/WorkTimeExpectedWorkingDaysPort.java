package com.gepardec.mega.hexagon.worktime.application.port.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

public interface WorkTimeExpectedWorkingDaysPort {
    Set<LocalDate> expectedWorkingDays(UserId userId, YearMonth month);
}
