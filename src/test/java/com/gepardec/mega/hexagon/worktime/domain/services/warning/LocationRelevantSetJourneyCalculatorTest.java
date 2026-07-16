package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.Vehicle;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static org.assertj.core.api.Assertions.assertThat;

class LocationRelevantSetJourneyCalculatorTest {

    private LocationRelevantSetJourneyCalculator calculator;

    @BeforeEach
    void init() {
        calculator = new LocationRelevantSetJourneyCalculator();
    }


    @Test
    void calculate_whenWorkTimeBookingWithWorkLocationIsProjectRelevantIsTrue_thenWarning() {
        JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO, true);
        WorkTimeBooking projectEntryTwo = projectTimeEntryFor(true);
        JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(9, 10, JourneyDirection.BACK, true);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntryOne, projectEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().type()).isNotNull();
        assertThat(warnings.getFirst().type()).isEqualTo(WorkTimeWarningType.LOCATION_RELEVANT_SET);
    }

    @Test
    void calculate_whenWorkTimeBookingWithWorkLocationIsProjectRelevantIsFalse_thenNoWarning() {
        JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO, false);
        WorkTimeBooking projectEntryTwo = projectTimeEntryFor(false);
        JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(9, 10, JourneyDirection.BACK, false);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntryOne, projectEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).isEmpty();
    }

    private ProjectBooking projectTimeEntryFor(Boolean workLocationIsProjectRelevant) {
        return WarningTestBookingBuilder.projectBookingBuilder()
                .fromTime(LocalDateTime.of(2020, 1, 7, 8, 0))
                .toTime(LocalDateTime.of(2020, 1, 7, 9, 0))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.A)
                .workLocationIsProjectRelevant(workLocationIsProjectRelevant)
                .build();
    }


    private JourneyBooking journeyTimeEntryFor(final int startHour, final int endHour,
                                               final JourneyDirection direction, Boolean workLocationIsProjectRelevant) {
        return WarningTestBookingBuilder.journeyBookingBuilder()
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
