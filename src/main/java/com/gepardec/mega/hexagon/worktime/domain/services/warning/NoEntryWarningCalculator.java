package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.NO_TIME_ENTRY;

public class NoEntryWarningCalculator {
    public List<WorkTimeWarning> calculate(
            Set<LocalDate> expectedWorkingDays,
            Set<LocalDate> bookedDates,
            Set<LocalDate> excusedDates,
            LocalDate today) {
        return expectedWorkingDays.stream()
                .filter(date -> !bookedDates.contains(date))
                .filter(date -> !excusedDates.contains(date))
                .filter(date -> date.isBefore(today))
                .sorted()
                .map(date -> new WorkTimeWarning(date, NO_TIME_ENTRY, null))
                .toList();
    }
}
