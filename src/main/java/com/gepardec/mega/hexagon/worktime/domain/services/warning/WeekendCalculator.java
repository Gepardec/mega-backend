package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.WEEKEND;

public class WeekendCalculator implements WorkTimeWarningCalculator {
    private static final Predicate<LocalDate> IS_SATURDAY = date -> DayOfWeek.SATURDAY.equals(date.getDayOfWeek());
    private static final Predicate<LocalDate> IS_SUNDAY = date -> DayOfWeek.SUNDAY.equals(date.getDayOfWeek());

    @Override
    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings) {
        return bookings.bookedDates().stream()
                .filter(IS_SATURDAY.or(IS_SUNDAY))
                .map(date -> new WorkTimeWarning(date, WEEKEND, null))
                .toList();
    }
}
