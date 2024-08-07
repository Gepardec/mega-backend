package com.gepardec.mega.domain.calculation.time;

import com.gepardec.mega.domain.calculation.AbstractTimeWarningCalculationStrategy;
import com.gepardec.mega.domain.calculation.WarningCalculationStrategy;
import com.gepardec.mega.domain.model.monthlyreport.JourneyTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectTimeEntry;
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
import java.util.function.Predicate;

/**
 * This calculator calculates the 'break time' on a single working day.
 * At least n hours 'break time' are necessary during one working day, whereby the break time can be distributed as intended.
 * {@link com.gepardec.mega.domain.model.monthlyreport.JourneyTimeEntry} are ignored for now, only {@link ProjectTimeEntry} are considered.
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

        final Map<LocalDate, List<ProjectEntry>> groupedProjectTimeEntries = groupProjectEntriesByFromDate(projectTimes, List.of(filterTask.or(filterActiveTravelTime)));

        for (final Map.Entry<LocalDate, List<ProjectEntry>> projectTimeEntry : groupedProjectTimeEntries.entrySet()) {

            final double workHoursOfDay = calculateWorkingDuration(projectTimeEntry.getValue());
            final List<ProjectEntry> entriesPerDay = projectTimeEntry.getValue();

            if (hasExceededMaximumWorkingHoursWithoutBreak(workHoursOfDay)) {
                final double breakTime = calculateBreakTimeSummaryForAllDayEntries(entriesPerDay);
                if (breakTime < MIN_REQUIRED_BREAK_TIME) {
                    warnings.add(createTimeWarning(projectTimeEntry.getKey(), breakTime));
                }
            }
        }
        return warnings;
    }

    private double calculateBreakTimeSummaryForAllDayEntries(final List<ProjectEntry> entriesPerDay) {
        BigDecimal breakTime = BigDecimal.ZERO;
        for (int i = 0; i < entriesPerDay.size(); i++) {
            final ProjectEntry actualEntry = entriesPerDay.get(i);
            final ProjectEntry nextEntry = getNextEntryOrNull(i, entriesPerDay);
            if (nextEntry != null && actualEntry.getToTime().isBefore(nextEntry.getFromTime())) {
                final BigDecimal currentBreakTime = BigDecimal.valueOf(
                                Duration.between(actualEntry.getToTime(), nextEntry.getFromTime()).toMinutes()
                        )
                        .setScale(2, RoundingMode.HALF_EVEN)
                        .divide(BigDecimal.valueOf(60), RoundingMode.HALF_EVEN);
                breakTime = breakTime.add(currentBreakTime);
            }
        }

        return breakTime.doubleValue();
    }

    private ProjectEntry getNextEntryOrNull(final int idx, final List<ProjectEntry> projectTimeEntries) {
        if (idx + 1 < projectTimeEntries.size()) {
            return projectTimeEntries.get(idx + 1);
        }
        return null;
    }

    private boolean hasExceededMaximumWorkingHoursWithoutBreak(final double workHoursOfDay) {
        return workHoursOfDay > MAX_HOURS_OF_DAY_WITHOUT_BREAK;
    }

    private TimeWarning createTimeWarning(final LocalDate date, final double breakTime) {
        TimeWarning timeWarning = new TimeWarning();
        timeWarning.setDate(date);
        timeWarning.setMissingBreakTime(
                BigDecimal.valueOf(MIN_REQUIRED_BREAK_TIME)
                        .subtract(BigDecimal.valueOf(breakTime))
                        .setScale(2, RoundingMode.HALF_EVEN)
                        .doubleValue());
        timeWarning.getWarningTypes().add(TimeWarningType.MISSING_BREAK_TIME);
        return timeWarning;
    }
}
