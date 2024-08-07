package com.gepardec.mega.domain.calculation.time;

import com.gepardec.mega.domain.calculation.AbstractTimeWarningCalculationStrategy;
import com.gepardec.mega.domain.calculation.WarningCalculationStrategy;
import com.gepardec.mega.domain.model.monthlyreport.JourneyTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.Task;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarningType;
import com.gepardec.mega.domain.model.monthlyreport.WarningType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ExceededMaximumWorkingHoursPerDayCalculator extends AbstractTimeWarningCalculationStrategy implements WarningCalculationStrategy<TimeWarning> {

    static final double MAX_HOURS_A_DAY = 10d;

    @Override
    public List<TimeWarning> calculate(List<ProjectEntry> projectTimeEntries) {
        final List<TimeWarning> warnings = new ArrayList<>(0);

        final Predicate<ProjectEntry> filterTask = entry -> Task.isTask(entry.getTask());
        final Predicate<ProjectEntry> filterActiveTravelTime =
                entry -> Task.isJourney(entry.getTask())
                        && ((JourneyTimeEntry) entry).getVehicle().activeTraveler;

        final Map<LocalDate, List<ProjectEntry>> groupedProjectTimeEntries = groupProjectEntriesByFromDate(projectTimeEntries, List.of(filterTask.or(filterActiveTravelTime)));

        for (final Map.Entry<LocalDate, List<ProjectEntry>> projectTimeEntry : groupedProjectTimeEntries.entrySet()) {

            final List<ProjectEntry> projectEntriesPerDay = projectTimeEntry.getValue();
            final double workDurationOfDay = calculateWorkingDuration(projectEntriesPerDay);

            if (hasExceededMaximumWorkingHoursPerDay(workDurationOfDay)) {
                warnings.add(createTimeWarning(projectTimeEntry.getKey(), workDurationOfDay));
            }
        }
        return warnings;
    }

    private boolean hasExceededMaximumWorkingHoursPerDay(final double workDurationOfDay) {
        return workDurationOfDay > MAX_HOURS_A_DAY;
    }

    private TimeWarning createTimeWarning(final LocalDate date, final double workDurationOfDay) {
        TimeWarning timeWarning = new TimeWarning();
        timeWarning.setDate(date);
        timeWarning.setExcessWorkTime(workDurationOfDay - MAX_HOURS_A_DAY);
        timeWarning.setWarningTypes(List.of(TimeWarningType.EXCESS_WORKING_TIME_PRESENT));
        return timeWarning;
    }
}
