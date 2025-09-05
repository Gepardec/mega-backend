package com.gepardec.mega.domain.calculation.journey;

import com.gepardec.mega.domain.calculation.WarningCalculationStrategy;
import com.gepardec.mega.domain.model.monthlyreport.JourneyDirection;
import com.gepardec.mega.domain.model.monthlyreport.JourneyTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarning;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarningType;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.Task;
import com.gepardec.mega.domain.model.monthlyreport.WorkingLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InvalidWorkingLocationInJourneyCalculator implements WarningCalculationStrategy<JourneyWarning> {

    public List<JourneyWarning> calculate(List<ProjectEntry> projectEntries) {
        final List<ProjectEntry> sortedProjectEntries = projectEntries.stream() //Sorting projectEntries by time
                .sorted(Comparator.comparing(ProjectEntry::getFromTime).thenComparing(ProjectEntry::getToTime))
                .toList();
        List<JourneyWarning> warnings = new ArrayList<>();

        if (!hasJourneyEntries(sortedProjectEntries)) {
            return warnings;
        }
        WorkingLocation workingLocation = WorkingLocation.MAIN; //setting to main, if a journey exceeds one month this must be changed

        for (final ProjectEntry projectEntry : sortedProjectEntries) {
            if (Task.isJourney(projectEntry.getTask())) {
                JourneyDirection journeyDirection = ((JourneyTimeEntry) projectEntry).getJourneyDirection();
                if (journeyDirection.equals(JourneyDirection.BACK)) {
                    workingLocation = WorkingLocation.MAIN;
                    continue;
                }
                if (journeyDirection.equals(JourneyDirection.TO) || journeyDirection.equals(JourneyDirection.FURTHER)) {
                    workingLocation = projectEntry.getWorkingLocation();
                    continue;
                }
            }
            if (!projectEntry.getWorkingLocation().equals(workingLocation)) {
                warnings.add(createJourneyWarningWithEnumType(projectEntry, JourneyWarningType.INVALID_WORKING_LOCATION));
            }
        }


        return warnings;
    }

    private boolean hasJourneyEntries(final List<ProjectEntry> projectEntries) {
        return projectEntries.stream().anyMatch(entry -> Task.isTask(entry.getTask()));
    }

    private JourneyWarning createJourneyWarningWithEnumType(ProjectEntry projectEntry, JourneyWarningType warning) {
        JourneyWarning newJourneyWarning = new JourneyWarning();
        newJourneyWarning.setDate(projectEntry.getDate());
        newJourneyWarning.getWarningTypes().add(warning);
        return newJourneyWarning;
    }
}
