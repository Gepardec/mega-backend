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

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.MISSING_BREAK_TIME;

public class InsufficientBreakCalculator implements WorkTimeWarningCalculator {
    private static final Duration MIN_REQUIRED_BREAK_TIME = Duration.ofMinutes(30);
    private static final Duration MAX_WORK_WITHOUT_BREAK = Duration.ofHours(6);

    @Override
    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings) {
        List<WorkTimeWarning> warnings = new ArrayList<>();
        bookings.contributingToWorkingTime().byDate().forEach((date, dayBookings) ->
                findBreakViolation(dayBookings)
                        .map(hours -> new WorkTimeWarning(date, MISSING_BREAK_TIME, hours))
                        .ifPresent(warnings::add));
        return warnings;
    }

    private Optional<Double> findBreakViolation(WorkTimeBookings bookings) {
        BreakProgress progress = BreakProgress.empty();
        for (var entry : Sequences.withSuccessor(bookings.values())) {
            WorkTimeBooking booking = entry.current();
            progress = progress.addWork(booking);
            if (progress.exceedsWorkLimit()) {
                return progress.missingBreakHours();
            }
            if (entry.next().isPresent()) {
                progress = progress.addBreakBetween(booking, entry.next().orElseThrow());
            }
        }
        return Optional.empty();
    }

    private record BreakProgress(Duration worked, Duration breakTime) {
        private BreakProgress {
            Objects.requireNonNull(worked, "worked must not be null");
            Objects.requireNonNull(breakTime, "breakTime must not be null");
        }

        private static BreakProgress empty() {
            return new BreakProgress(Duration.ZERO, Duration.ZERO);
        }

        private BreakProgress addWork(WorkTimeBooking booking) {
            long workedMinutes = Duration.between(booking.from(), booking.to()).toMinutes();
            return new BreakProgress(worked.plusMinutes(workedMinutes), breakTime);
        }

        private BreakProgress addBreakBetween(WorkTimeBooking current, WorkTimeBooking next) {
            if (!current.to().isBefore(next.from())) {
                return this;
            }
            return new BreakProgress(worked, breakTime.plus(Duration.between(current.to(), next.from())));
        }

        private boolean exceedsWorkLimit() {
            return worked.compareTo(MAX_WORK_WITHOUT_BREAK) > 0;
        }

        private Optional<Double> missingBreakHours() {
            if (breakTime.compareTo(MIN_REQUIRED_BREAK_TIME) >= 0) {
                return Optional.empty();
            }
            long missingMinutes = MIN_REQUIRED_BREAK_TIME.minus(breakTime).toMinutes();
            return Optional.of(BigDecimal.valueOf(missingMinutes)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_EVEN)
                    .doubleValue());
        }
    }
}
