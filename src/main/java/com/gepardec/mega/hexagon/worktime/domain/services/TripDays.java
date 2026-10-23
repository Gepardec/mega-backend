package com.gepardec.mega.hexagon.worktime.domain.services;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Determines the days of a payroll month that belong to a business trip. The journeys must be in chronological order.
 */
public final class TripDays {

    private TripDays() {
    }

    public static Set<LocalDate> of(List<JourneyBooking> journeys, YearMonth month) {
        Set<LocalDate> tripDays = new TreeSet<>();
        LocalDate openTripStart = null;

        for (int i = 0; i < journeys.size(); i++) {
            JourneyBooking journey = journeys.get(i);
            LocalDate date = journey.date();
            boolean firstOfMonth = i == 0;
            tripDays.add(date);

            switch (journey.direction()) {
                case TO -> openTripStart = date;
                case FURTHER -> {
                    if (openTripStart == null && firstOfMonth) {
                        openTripStart = month.atDay(1);
                    }
                }
                case BACK -> {
                    if (openTripStart != null) {
                        addRange(tripDays, openTripStart, date);
                        openTripStart = null;
                    } else if (firstOfMonth) {
                        addRange(tripDays, month.atDay(1), date);
                    }
                }
            }
        }

        if (openTripStart != null) {
            addRange(tripDays, openTripStart, month.atEndOfMonth());
        }
        return Collections.unmodifiableSet(tripDays);
    }

    private static void addRange(Set<LocalDate> days, LocalDate fromInclusive, LocalDate toInclusive) {
        fromInclusive.datesUntil(toInclusive.plusDays(1)).forEach(days::add);
    }
}
