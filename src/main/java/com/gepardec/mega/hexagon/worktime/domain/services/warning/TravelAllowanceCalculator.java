package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.services.TravelAllowancePolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.TRAVEL_ALLOWANCE_EXCEEDED;
import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.TRAVEL_ALLOWANCE_WRONG_PROJECT;

public class TravelAllowanceCalculator {
    private static final LocalDate RULE_START = LocalDate.of(2026, 10, 1);
    /** ZEP process "Reisepauschale" of the project "Intern". */
    private static final String REISEPAUSCHALE_PROCESS = "873";

    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings, Set<Integer> billableProjectZepIds) {
        return bookings.byDate().entrySet().stream()
                .filter(day -> !day.getKey().isBefore(RULE_START))
                .flatMap(day -> evaluate(day.getKey(), day.getValue(), billableProjectZepIds).stream())
                .toList();
    }

    private static Optional<WorkTimeWarning> evaluate(LocalDate date, WorkTimeBookings dayBookings,
                                                      Set<Integer> billableProjectZepIds) {
        List<ProjectBooking> allowanceBookings = dayBookings.projects().stream()
                .filter(booking -> REISEPAUSCHALE_PROCESS.equals(booking.process()))
                .toList();
        if (allowanceBookings.isEmpty()) {
            return Optional.empty();
        }

        List<JourneyBooking> passiveJourneys = dayBookings.journeys().stream()
                .filter(journey -> !journey.vehicle().isActiveTraveler())
                .toList();
        List<JourneyBooking> internalTrips = passiveJourneys.stream()
                .filter(journey -> !isCustomerTrip(journey, billableProjectZepIds))
                .toList();
        if (!passiveJourneys.isEmpty() && internalTrips.isEmpty()) {
            return Optional.of(new WorkTimeWarning(date, TRAVEL_ALLOWANCE_WRONG_PROJECT, null));
        }

        Duration booked = allowanceBookings.stream()
                .map(booking -> Duration.between(booking.from(), booking.to()))
                .reduce(Duration.ZERO, Duration::plus);
        Duration allowed = internalTrips.stream()
                .map(TravelAllowancePolicy::allowanceFor)
                .reduce(Duration.ZERO, Duration::plus);
        Duration excess = booked.minus(allowed);

        return excess.isPositive()
                ? Optional.of(new WorkTimeWarning(date, TRAVEL_ALLOWANCE_EXCEEDED, toHours(excess)))
                : Optional.empty();
    }

    private static boolean isCustomerTrip(JourneyBooking journey, Set<Integer> billableProjectZepIds) {
        return journey.projectZepId() != null && billableProjectZepIds.contains(journey.projectZepId());
    }

    private static double toHours(Duration duration) {
        return BigDecimal.valueOf(duration.toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_EVEN)
                .doubleValue();
    }
}
