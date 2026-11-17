package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.TravelAllowanceTasks;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEvaluation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.services.TravelAllowancePolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.TRAVEL_ALLOWANCE_EXCEEDED;
import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.TRAVEL_ALLOWANCE_WRONG_PROJECT;

public class TravelAllowanceCalculator implements WorkTimeWarningCalculator {
    private static final String INTERN_PROCESS = String.valueOf(TravelAllowanceTasks.INTERN_REISEPAUSCHALE_TASK_ID);

    @Override
    public WarningRule rule() {
        return WarningRule.TRAVEL_ALLOWANCE;
    }

    @Override
    public List<WorkTimeWarning> calculate(WorkTimeEvaluation evaluation) {
        TravelAllowanceTasks tasks = evaluation.travelAllowanceTasks();
        return evaluation.bookings().byDate().entrySet().stream()
                .flatMap(day -> evaluate(day.getKey(), day.getValue(), tasks).stream())
                .toList();
    }

    private static List<WorkTimeWarning> evaluate(LocalDate date, WorkTimeBookings dayBookings, TravelAllowanceTasks tasks) {
        Map<Place, Duration> booked = new HashMap<>();
        for (ProjectBooking booking : dayBookings.projects()) {
            Place place = placeOf(booking, tasks);
            if (place != null) {
                booked.merge(place, Duration.between(booking.from(), booking.to()), Duration::plus);
            }
        }
        if (booked.isEmpty()) {
            return List.of();
        }

        Map<Place, Duration> allowed = new HashMap<>();
        boolean journeyOnOwnTask = false;
        for (JourneyBooking journey : dayBookings.journeys()) {
            if (journey.vehicle().isActiveTraveler()) {
                continue;
            }
            Place place = placeOf(journey, tasks);
            journeyOnOwnTask |= place.isProject();
            allowed.merge(place, TravelAllowancePolicy.allowanceFor(journey), Duration::plus);
        }

        Duration internExcess = excess(booked, allowed, Place.INTERN);
        boolean wrongProject = internExcess.isPositive() && journeyOnOwnTask;

        Set<Place> projectPlaces = new HashSet<>(booked.keySet());
        projectPlaces.remove(Place.INTERN);
        Duration excess = projectPlaces.stream()
                .map(place -> excess(booked, allowed, place))
                .reduce(Duration.ZERO, Duration::plus);
        if (!wrongProject) {
            excess = excess.plus(internExcess);
        }

        List<WorkTimeWarning> warnings = new ArrayList<>();
        if (wrongProject) {
            warnings.add(new WorkTimeWarning(date, TRAVEL_ALLOWANCE_WRONG_PROJECT, null));
        }
        if (excess.isPositive()) {
            warnings.add(new WorkTimeWarning(date, TRAVEL_ALLOWANCE_EXCEEDED, toHours(excess)));
        }
        return warnings;
    }

    private static Duration excess(Map<Place, Duration> booked, Map<Place, Duration> allowed, Place place) {
        Duration over = booked.getOrDefault(place, Duration.ZERO).minus(allowed.getOrDefault(place, Duration.ZERO));
        return over.isPositive() ? over : Duration.ZERO;
    }

    private static Place placeOf(JourneyBooking journey, TravelAllowanceTasks tasks) {
        return tasks.declaresOwnTask(journey.projectZepId()) ? Place.project(journey.projectZepId()) : Place.INTERN;
    }

    private static Place placeOf(ProjectBooking booking, TravelAllowanceTasks tasks) {
        if (INTERN_PROCESS.equals(booking.process())) {
            return Place.INTERN;
        }
        if (booking.projectZepId() != null && booking.process() != null
                && tasks.ownTaskIds(booking.projectZepId()).stream().anyMatch(id -> String.valueOf(id).equals(booking.process()))) {
            return Place.project(booking.projectZepId());
        }
        return null;
    }

    private record Place(Integer projectZepId) {
        static final Place INTERN = new Place(null);

        static Place project(Integer projectZepId) {
            return new Place(projectZepId);
        }

        boolean isProject() {
            return projectZepId != null;
        }
    }

    private static double toHours(Duration duration) {
        return BigDecimal.valueOf(duration.toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_EVEN)
                .doubleValue();
    }
}
