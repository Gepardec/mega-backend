package com.gepardec.mega.domain.calculation.journey;

import com.gepardec.mega.domain.calculation.WarningCalculationStrategy;
import com.gepardec.mega.domain.model.monthlyreport.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LocationRelevantSetJourneyCalculator implements WarningCalculationStrategy<JourneyWarning> {

    @Override
    public List<JourneyWarning> calculate(List<ProjectEntry> projectEntries) {
        final List<JourneyWarning> warnings = new ArrayList<>();

        for (ProjectEntry projectEntry : projectEntries) {
            if (projectEntry.getWorkLocationIsProjectRelevant()) {
                if (warnings.stream().noneMatch(e -> e.getDate().equals(projectEntry.getDate()))) {
                    warnings.add(createJourneyWarningWithEnumType(projectEntry, JourneyWarningType.LOCATION_RELEVANT_SET));
                }
            }
        }
        return warnings;
    }

    private JourneyWarning createJourneyWarningWithEnumType(ProjectEntry projectEntry, JourneyWarningType warning) {
        JourneyWarning newJourneyWarning = new JourneyWarning();
        newJourneyWarning.setDate(projectEntry.getDate());
        newJourneyWarning.getWarningTypes().add(warning);
        return newJourneyWarning;
    }
}
