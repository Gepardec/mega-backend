package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.services.TripDays;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.MEAL_ENTRY_MISSING;

public class MealEntryCalculator {
    private static final LocalDate RULE_START = LocalDate.of(2026, 10, 1);

    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings, YearMonth month,
                                           Set<LocalDate> mealEntryDates, LocalDate today) {
        return TripDays.of(bookings.journeys(), month).stream()
                .filter(date -> !date.isBefore(RULE_START))
                .filter(date -> date.isBefore(today))
                .filter(date -> !mealEntryDates.contains(date))
                .sorted()
                .map(date -> new WorkTimeWarning(date, MEAL_ENTRY_MISSING, null))
                .toList();
    }
}
