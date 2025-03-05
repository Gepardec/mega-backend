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

    private ProjectTimeEntry projectTimeEntryFor(final int startHour, final int endHour, Boolean workLocationIsProjectRelevant) {
        return projectTimeEntryFor(startHour, 0, endHour, 0, workLocationIsProjectRelevant);
    }

    private ProjectTimeEntry projectTimeEntryFor(final int startHour, final int startMinute, final int endHour, final int endMinute,
                                                 Boolean workLocationIsProjectRelevant) {
        return ProjectTimeEntry.builder()
                .fromTime(LocalDateTime.of(2020, 1, 7, startHour, startMinute))
                .toTime(LocalDateTime.of(2020, 1, 7, endHour, endMinute))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.A)
                .workLocationIsProjectRelevant(workLocationIsProjectRelevant)
                .build();
    }

    private JourneyTimeEntry journeyTimeEntryFor(final int startHour, final int endHour, final JourneyDirection direction,
                                                 Boolean workLocationIsProjectRelevant) {
        return journeyTimeEntryFor(startHour, 0, endHour, 0, direction, workLocationIsProjectRelevant);
    }

    private JourneyTimeEntry journeyTimeEntryFor(final int startHour, final int startMinute, final int endHour, final int endMinute,
                                                 final JourneyDirection direction, Boolean workLocationIsProjectRelevant) {
        return JourneyTimeEntry.builder()
                .fromTime(LocalDateTime.of(2020, 1, 7, startHour, startMinute))
                .toTime(LocalDateTime.of(2020, 1, 7, endHour, endMinute))
                .task(Task.REISEN)
                .workingLocation(WorkingLocation.A)
                .journeyDirection(direction)
                .vehicle(Vehicle.OTHER_INACTIVE)
                .workLocationIsProjectRelevant(workLocationIsProjectRelevant)
                .build();
    }

    @Test
    void whenProjectEntryWithWorkLocationIsProjectRelevantIsTrue_thenWarning() {
        JourneyTimeEntry journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO,null);
        ProjectEntry projectEntryTwo = projectTimeEntryFor(8, 9,null);
        JourneyTimeEntry journeyTimeEntryThree = journeyTimeEntryFor(9, 10, JourneyDirection.BACK,null);

        List<JourneyWarning> warnings = calculator.calculate(List.of(journeyTimeEntryOne, projectEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.get(0).getWarningTypes()).hasSize(1);
        assertThat(warnings.get(0).getWarningTypes().get(0)).isEqualTo(JourneyWarningType.LOCATION_RELEVANT_SET);
    }

    @Test
    void whenProjectEntryWithWorkLocationIsProjectRelevantIsFalse_thenNoWarning() {
        JourneyTimeEntry journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO,true);
        ProjectEntry projectEntryTwo = projectTimeEntryFor(8, 9,true);
        JourneyTimeEntry journeyTimeEntryThree = journeyTimeEntryFor(9, 10, JourneyDirection.BACK,true);

        List<JourneyWarning> warnings = calculator.calculate(List.of(journeyTimeEntryOne, projectEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).isEmpty();
    }
}
