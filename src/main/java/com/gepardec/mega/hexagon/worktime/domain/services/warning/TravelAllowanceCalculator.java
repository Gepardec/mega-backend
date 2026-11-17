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
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private static List<WorkTimeWarning> evaluate(LocalDate date, WorkTimeBookings day, TravelAllowanceTasks tasks) {
        Map<Place, Duration> booked = bookedPerPlace(day, tasks);
        Map<Place, Duration> allowed = allowedPerPlace(day, tasks);
        Map<Place, Duration> excess = excessPerPlace(booked, allowed);

        boolean wrongProject = excess.containsKey(Place.INTERN) && allowed.keySet().stream().anyMatch(Place::isProject);
        if (wrongProject) {
            excess.remove(Place.INTERN);
        }
        Duration totalExcess = excess.values().stream().reduce(Duration.ZERO, Duration::plus);

        List<WorkTimeWarning> warnings = new ArrayList<>();
        if (wrongProject) {
            warnings.add(new WorkTimeWarning(date, TRAVEL_ALLOWANCE_WRONG_PROJECT, null));
        }
        if (totalExcess.isPositive()) {
            warnings.add(new WorkTimeWarning(date, TRAVEL_ALLOWANCE_EXCEEDED, toHours(totalExcess)));
        }
        return warnings;
    }

    private static Map<Place, Duration> bookedPerPlace(WorkTimeBookings day, TravelAllowanceTasks tasks) {
        Map<Place, Duration> booked = new HashMap<>();
        for (ProjectBooking booking : day.projects()) {
            placeOf(booking, tasks).ifPresent(place ->
                    booked.merge(place, Duration.between(booking.from(), booking.to()), Duration::plus));
        }
        return booked;
    }

    private static Map<Place, Duration> allowedPerPlace(WorkTimeBookings day, TravelAllowanceTasks tasks) {
        Map<Place, Duration> allowed = new HashMap<>();
        for (JourneyBooking journey : day.journeys()) {
            if (!journey.vehicle().isActiveTraveler()) {
                allowed.merge(placeOf(journey, tasks), TravelAllowancePolicy.allowanceFor(journey), Duration::plus);
            }
        }
        return allowed;
    }

    private static Map<Place, Duration> excessPerPlace(Map<Place, Duration> booked, Map<Place, Duration> allowed) {
        Map<Place, Duration> excess = new HashMap<>();
        booked.forEach((place, bookedDuration) -> {
            Duration over = bookedDuration.minus(allowed.getOrDefault(place, Duration.ZERO));
            if (over.isPositive()) {
                excess.put(place, over);
            }
        });
        return excess;
    }

    private static Place placeOf(JourneyBooking journey, TravelAllowanceTasks tasks) {
        return tasks.declaresOwnTask(journey.projectZepId()) ? Place.project(journey.projectZepId()) : Place.INTERN;
    }

    private static Optional<Place> placeOf(ProjectBooking booking, TravelAllowanceTasks tasks) {
        if (INTERN_PROCESS.equals(booking.process())) {
            return Optional.of(Place.INTERN);
        }
        boolean onOwnTask = tasks.ownTaskIds(booking.projectZepId()).stream()
                .anyMatch(taskId -> String.valueOf(taskId).equals(booking.process()));
        return onOwnTask ? Optional.of(Place.project(booking.projectZepId())) : Optional.empty();
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
