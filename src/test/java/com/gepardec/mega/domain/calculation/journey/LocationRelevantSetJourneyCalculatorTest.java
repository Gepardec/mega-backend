package com.gepardec.mega.domain.calculation.journey;

import com.gepardec.mega.domain.model.monthlyreport.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocationRelevantSetJourneyCalculatorTest {

    private LocationRelevantSetJourneyCalculator calculator;

    @BeforeEach
    void init() {
        calculator = new LocationRelevantSetJourneyCalculator();
    }


    @Test
    void calculate_whenProjectEntryWithWorkLocationIsProjectRelevantIsTrue_thenWarning() {
        JourneyTimeEntry journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO,true);
        ProjectEntry projectEntryTwo = projectTimeEntryFor(true);
        JourneyTimeEntry journeyTimeEntryThree = journeyTimeEntryFor(9, 10, JourneyDirection.BACK,true);

        List<JourneyWarning> warnings = calculator.calculate(List.of(journeyTimeEntryOne, projectEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().getWarningTypes()).hasSize(1);
        assertThat(warnings.getFirst().getWarningTypes().getFirst()).isEqualTo(JourneyWarningType.LOCATION_RELEVANT_SET);
    }

    @Test
    void calculate_whenProjectEntryWithWorkLocationIsProjectRelevantIsFalse_thenNoWarning() {
        JourneyTimeEntry journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO,false);
        ProjectEntry projectEntryTwo = projectTimeEntryFor(false);
        JourneyTimeEntry journeyTimeEntryThree = journeyTimeEntryFor(9, 10, JourneyDirection.BACK,false);

        List<JourneyWarning> warnings = calculator.calculate(List.of(journeyTimeEntryOne, projectEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).isEmpty();
    }

    private ProjectTimeEntry projectTimeEntryFor(Boolean workLocationIsProjectRelevant) {
        return ProjectTimeEntry.builder()
                .fromTime(LocalDateTime.of(2020, 1, 7, 8, 0))
                .toTime(LocalDateTime.of(2020, 1, 7, 9, 0))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.A)
                .workLocationIsProjectRelevant(workLocationIsProjectRelevant)
                .build();
    }


    private JourneyTimeEntry journeyTimeEntryFor(final int startHour, final int endHour,
                                                 final JourneyDirection direction, Boolean workLocationIsProjectRelevant) {
        return JourneyTimeEntry.builder()
                .fromTime(LocalDateTime.of(2020, 1, 7, startHour, 0))
                .toTime(LocalDateTime.of(2020, 1, 7, endHour, 0))
                .task(Task.REISEN)
                .workingLocation(WorkingLocation.A)
                .journeyDirection(direction)
                .vehicle(Vehicle.OTHER_INACTIVE)
                .workLocationIsProjectRelevant(workLocationIsProjectRelevant)
                .build();
    }
}
