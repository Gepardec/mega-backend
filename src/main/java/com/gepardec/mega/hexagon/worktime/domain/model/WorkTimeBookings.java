package com.gepardec.mega.hexagon.worktime.domain.model;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record WorkTimeBookings(List<WorkTimeBooking> values) implements Iterable<WorkTimeBooking> {
    private static final Comparator<WorkTimeBooking> CHRONOLOGICAL_ORDER = Comparator
            .comparing(WorkTimeBooking::from)
            .thenComparing(WorkTimeBooking::to);

    public WorkTimeBookings {
        Objects.requireNonNull(values, "values must not be null");
        if (values.stream().anyMatch(Objects::isNull)) {
            throw new NullPointerException("values must not contain null");
        }
        values = values.stream()
                .sorted(CHRONOLOGICAL_ORDER)
                .toList();
    }

    public static WorkTimeBookings empty() {
        return new WorkTimeBookings(List.of());
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public Iterator<WorkTimeBooking> iterator() {
        return values.iterator();
    }

    public Map<LocalDate, WorkTimeBookings> byDate() {
        Map<LocalDate, List<WorkTimeBooking>> groupedBookings = new LinkedHashMap<>();
        for (WorkTimeBooking booking : values) {
            groupedBookings.computeIfAbsent(booking.date(), ignored -> new ArrayList<>()).add(booking);
        }

        Map<LocalDate, WorkTimeBookings> result = new LinkedHashMap<>();
        groupedBookings.forEach((date, bookings) -> result.put(date, new WorkTimeBookings(bookings)));
        return Collections.unmodifiableMap(result);
    }

    public WorkTimeBookings contributingToWorkingTime() {
        return new WorkTimeBookings(
                values.stream()
                        .filter(WorkTimeBookings::contributesToWorkingTime)
                        .toList()
        );
    }

    public List<ProjectBooking> projects() {
        return values.stream()
                .filter(ProjectBooking.class::isInstance)
                .map(ProjectBooking.class::cast)
                .toList();
    }

    public List<JourneyBooking> journeys() {
        return values.stream()
                .filter(JourneyBooking.class::isInstance)
                .map(JourneyBooking.class::cast)
                .toList();
    }

    public Set<LocalDate> bookedDates() {
        Set<LocalDate> dates = new LinkedHashSet<>();
        values.forEach(booking -> dates.add(booking.date()));
        return Collections.unmodifiableSet(dates);
    }

    public Duration totalDuration() {
        return values.stream()
                .map(booking -> Duration.between(booking.from(), booking.to()))
                .reduce(Duration.ZERO, Duration::plus);
    }

    private static boolean contributesToWorkingTime(WorkTimeBooking booking) {
        return switch (booking) {
            case ProjectBooking ignored -> true;
            case JourneyBooking journey -> journey.vehicle().isActiveTraveler();
        };
    }
}
