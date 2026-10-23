package com.gepardec.mega.hexagon.worktime.domain.services;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.KnownPlace;

import java.time.Duration;
import java.util.Set;

import static com.gepardec.mega.hexagon.worktime.domain.model.KnownPlace.LINZ;
import static com.gepardec.mega.hexagon.worktime.domain.model.KnownPlace.ST_POELTEN;
import static com.gepardec.mega.hexagon.worktime.domain.model.KnownPlace.WIEN;

public final class TravelAllowancePolicy {
    private static final Duration SHORT_ROUTE_ALLOWANCE = Duration.ofMinutes(15);
    private static final Duration ORDINARY_ROUTE_ALLOWANCE = Duration.ofMinutes(30);
    private static final Set<Set<KnownPlace>> SHORT_ROUTES = Set.of(
            Set.of(ST_POELTEN, WIEN),
            Set.of(ST_POELTEN, LINZ));

    private TravelAllowancePolicy() {
    }

    public static Duration allowanceFor(JourneyBooking journey) {
        if (journey.vehicle().isActiveTraveler()) {
            return Duration.ZERO;
        }
        return isShortRoute(journey) ? SHORT_ROUTE_ALLOWANCE : ORDINARY_ROUTE_ALLOWANCE;
    }

    private static boolean isShortRoute(JourneyBooking journey) {
        var start = KnownPlace.recognise(journey.start());
        var destination = KnownPlace.recognise(journey.destination());
        return start.isPresent() && destination.isPresent()
                && !start.equals(destination)
                && SHORT_ROUTES.contains(Set.of(start.get(), destination.get()));
    }
}
