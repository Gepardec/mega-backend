package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.services.Sequences;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.MISSING_REST_TIME;

public class InsufficientRestCalculator implements WorkTimeWarningCalculator {
    private static final Duration MIN_REQUIRED_REST_TIME = Duration.ofHours(11);

    @Override
    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings) {
        List<WorkTimeBooking> relevant = bookings.contributingToWorkingTime().values();
        List<WorkTimeWarning> warnings = new ArrayList<>();
        for (var entry : Sequences.withSuccessor(relevant)) {
            Optional<WorkTimeBooking> maybeNext = entry.next();
            if (maybeNext.isEmpty()) {
                break;
            }
            WorkTimeBooking current = entry.current();
            WorkTimeBooking next = maybeNext.get();
            if (next.date().equals(current.date().plusDays(1))) {
                RestPeriod restPeriod = RestPeriod.between(current, next);
                if (restPeriod.isInsufficient()) {
                    warnings.add(new WorkTimeWarning(
                            next.date(), MISSING_REST_TIME, restPeriod.missingHours()));
                }
            }
        }
        return warnings;
    }

    private record RestPeriod(Duration duration) {
        private RestPeriod {
            Objects.requireNonNull(duration, "duration must not be null");
        }

        private static RestPeriod between(WorkTimeBooking current, WorkTimeBooking next) {
            return new RestPeriod(Duration.between(current.to(), next.from()));
        }

        private boolean isInsufficient() {
            return roundedHours().compareTo(toRoundedHours(MIN_REQUIRED_REST_TIME)) < 0;
        }

        private double missingHours() {
            return toRoundedHours(MIN_REQUIRED_REST_TIME)
                    .subtract(roundedHours())
                    .setScale(2, RoundingMode.HALF_EVEN)
                    .doubleValue();
        }

        private BigDecimal roundedHours() {
            return toRoundedHours(duration);
        }

        private static BigDecimal toRoundedHours(Duration duration) {
            return BigDecimal.valueOf(duration.toMinutes())
                    .setScale(2, RoundingMode.HALF_EVEN)
                    .divide(BigDecimal.valueOf(60), RoundingMode.HALF_EVEN);
        }
    }
}
