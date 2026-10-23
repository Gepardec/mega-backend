package com.gepardec.mega.hexagon.worktime.application.port.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

public interface WorkTimeMealEntryZepPort {

    Set<LocalDate> fetchMealEntryDates(ZepUsername zepUsername, YearMonth month);
}
