package com.gepardec.mega.domain.calculation.time;

import com.gepardec.mega.domain.calculation.AbstractTimeWarningCalculationStrategy;
import com.gepardec.mega.domain.calculation.WarningCalculationStrategy;
import com.gepardec.mega.domain.model.monthlyreport.JourneyTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.Task;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarningType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Warns when an employee exceeds 6 hours of work without having accumulated at least 30 minutes
 * of break by that point. Breaks split across multiple gaps are summed. Inactive journey entries
 * are excluded.
 */
public class InsufficientBreakCalculator extends AbstractTimeWarningCalculationStrategy
        implements WarningCalculationStrategy<TimeWarning> {

    static final double MIN_REQUIRED_BREAK_TIME = 0.5d;
    static final double MAX_HOURS_OF_DAY_WITHOUT_BREAK = 6d;

    @Override
    public List<TimeWarning> calculate(List<ProjectEntry> projectTimes) {
        final List<TimeWarning> warnings = new ArrayList<>();

        final Predicate<ProjectEntry> filterTask = entry -> Task.isTask(entry.getTask());
        final Predicate<ProjectEntry> filterActiveTravelTime =
                entry -> Task.isJourney(entry.getTask())
                        && ((JourneyTimeEntry) entry).getVehicle().activeTraveler;

        final Map<LocalDate, List<ProjectEntry>> groupedByDate =
                groupProjectEntriesByFromDate(projectTimes, List.of(filterTask.or(filterActiveTravelTime)));

        for (final Map.Entry<LocalDate, List<ProjectEntry>> dayEntry : groupedByDate.entrySet()) {
            findBreakViolation(dayEntry.getValue())
                    .map(breakAtViolation -> createTimeWarning(dayEntry.getKey(), breakAtViolation))
                    .ifPresent(warnings::add);
        }
        return warnings;
    }

    /**
     * Walks entries in chronological order, accumulating work and break time. Returns the break
     * accumulated at the moment work first exceeds 6 hours, if that break is below 30 minutes.
     * A break taken only after the 6-hour mark does not satisfy the requirement.
     */
    private Optional<BigDecimal> findBreakViolation(List<ProjectEntry> entries) {
        double totalWork = 0;
        BigDecimal accumulatedBreak = BigDecimal.ZERO;
        ProjectEntry previous = null;

        for (ProjectEntry entry : entries) {
            if (previous != null && previous.getToTime().isBefore(entry.getFromTime())) {
                accumulatedBreak = accumulatedBreak.add(toBreakHours(
                        Duration.between(previous.getToTime(), entry.getFromTime()).toMinutes()
                ));
            }
            totalWork += entry.getDurationInHours();
            if (totalWork > MAX_HOURS_OF_DAY_WITHOUT_BREAK) {
                return accumulatedBreak.compareTo(BigDecimal.valueOf(MIN_REQUIRED_BREAK_TIME)) < 0
                        ? Optional.of(accumulatedBreak)
                        : Optional.empty();
            }
            previous = entry;
        }
        return Optional.empty();
    }

    private BigDecimal toBreakHours(long minutes) {
        return BigDecimal.valueOf(minutes)
                .setScale(2, RoundingMode.HALF_EVEN)
                .divide(BigDecimal.valueOf(60), RoundingMode.HALF_EVEN);
    }

    private TimeWarning createTimeWarning(final LocalDate date, final BigDecimal breakAtViolation) {
        TimeWarning timeWarning = new TimeWarning();
        timeWarning.setDate(date);
        timeWarning.setMissingBreakTime(
                BigDecimal.valueOf(MIN_REQUIRED_BREAK_TIME)
                        .subtract(breakAtViolation)
                        .setScale(2, RoundingMode.HALF_EVEN)
                        .doubleValue()
        );
        timeWarning.getWarningTypes().add(TimeWarningType.MISSING_BREAK_TIME);
        return timeWarning;
    }
}
