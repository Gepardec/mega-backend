package com.gepardec.mega.domain.calculation.journey;

import com.gepardec.mega.domain.calculation.WarningCalculationStrategy;
import com.gepardec.mega.domain.model.monthlyreport.*;

import java.util.ArrayList;
import java.util.List;

public class LocationRelevantSetJourneyCalculator implements WarningCalculationStrategy<JourneyWarning> {

    @Override
    public List<JourneyWarning> calculate(List<ProjectEntry> projectEntries) {
        final List<JourneyWarning> warnings = new ArrayList<>();

        for (ProjectEntry projectEntry : projectEntries) {
            if (projectEntry.getWorkLocationIsProjectRelevant() &&
                    warnings.stream().noneMatch(e -> e.getDate().equals(projectEntry.getDate()))) {
                    warnings.add(createJourneyWarning(projectEntry));
                }
        }
        return warnings;
    }

    private JourneyWarning createJourneyWarning(ProjectEntry projectEntry) {
        JourneyWarning newJourneyWarning = new JourneyWarning();
        newJourneyWarning.setDate(projectEntry.getDate());
        newJourneyWarning.getWarningTypes().add(JourneyWarningType.LOCATION_RELEVANT_SET);
        return newJourneyWarning;
    }
}
