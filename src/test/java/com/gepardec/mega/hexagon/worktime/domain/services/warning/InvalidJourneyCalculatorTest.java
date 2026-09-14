package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.Vehicle;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static org.assertj.core.api.Assertions.assertThat;

class InvalidJourneyCalculatorTest {

    private InvalidJourneyCalculator calculator;

    @BeforeEach
    void beforeEach() {
        calculator = new InvalidJourneyCalculator();
    }

    private ProjectBooking projectTimeEntryFor(final int startHour, final int endHour) {
        return projectTimeEntryFor(startHour, 0, endHour, 0);
    }

    private ProjectBooking projectTimeEntryFor(final int startHour, final int startMinute, final int endHour, final int endMinute) {
        return WarningTestBookingBuilder.projectBookingBuilder()
                .fromTime(LocalDateTime.of(2020, 1, 7, startHour, startMinute))
                .toTime(LocalDateTime.of(2020, 1, 7, endHour, endMinute))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
                .build();
    }

    private JourneyBooking journeyTimeEntryFor(final int startHour, final int endHour, final JourneyDirection direction,
                                               final WorkingLocation workingLocation) {
        return journeyTimeEntryFor(startHour, 0, endHour, 0, direction, workingLocation);
    }

    private JourneyBooking journeyTimeEntryFor(final int startHour, final int startMinute, final int endHour, final int endMinute,
                                               final JourneyDirection direction, final WorkingLocation workingLocation) {
        return WarningTestBookingBuilder.journeyBookingBuilder()
                .fromTime(LocalDateTime.of(2020, 1, 7, startHour, startMinute))
                .toTime(LocalDateTime.of(2020, 1, 7, endHour, endMinute))
                .task(Task.REISEN)
                .workingLocation(workingLocation)
                .journeyDirection(direction)
                .vehicle(Vehicle.OTHER_INACTIVE)
                .build();
    }

    @Test
    void whenOnlyDeparture_thenWarning() {
        final JourneyBooking journeyTimeEntry = journeyTimeEntryFor(8, 9, JourneyDirection.TO, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntry));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().type()).isNotNull();
        assertThat(warnings.getFirst().type()).isEqualTo(WorkTimeWarningType.BACK_MISSING);

    }

    @Test
    void whenDepartureAndProjectBooking_thenWarning() {
        final JourneyBooking journeyTimeEntry = journeyTimeEntryFor(1, 8, JourneyDirection.TO, WorkingLocation.MAIN);
        final ProjectBooking projectTimeEntry = projectTimeEntryFor(8, 10);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntry, projectTimeEntry));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().type()).isNotNull();
        assertThat(warnings.getFirst().type()).isEqualTo(WorkTimeWarningType.BACK_MISSING);
    }

    @Test
    void whenFurtherAndProjectBookingAndArrival_thenWarning() {
        final JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(1, 8, JourneyDirection.FURTHER, WorkingLocation.MAIN);
        final ProjectBooking projectTimeEntryTwo = projectTimeEntryFor(8, 10);
        final JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(10, 12, JourneyDirection.BACK, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntryOne, projectTimeEntryTwo, journeyTimeEntryThree));

        // TODO: Both journey entries cause TO_MISSING Warning, because all are checked separately
        assertThat(warnings).hasSize(2);
        assertThat(warnings.getFirst().type()).isNotNull();
        assertThat(warnings.getFirst().type()).isEqualTo(WorkTimeWarningType.TO_MISSING);
        assertThat(warnings.get(1).type()).isEqualTo(WorkTimeWarningType.TO_MISSING);
    }

    @Test
    void whenDepartureAndProjectBookingAndDepartureAgain_thenWarning() {
        final JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(1, 8, JourneyDirection.TO, WorkingLocation.MAIN);
        final ProjectBooking projectTimeEntryTwo = projectTimeEntryFor(8, 10);
        final JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(10, 12, JourneyDirection.TO, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntryOne, projectTimeEntryTwo, journeyTimeEntryThree));

        // TODO: Both journey entries cause BACK_MISSING Warning, because all are checked separately
        assertThat(warnings).hasSize(2);
        assertThat(warnings.getFirst().type()).isNotNull();
        assertThat(warnings.getFirst().type()).isEqualTo(WorkTimeWarningType.BACK_MISSING);
        assertThat(warnings.get(1).type()).isEqualTo(WorkTimeWarningType.BACK_MISSING);
    }

    @Test
    void whenArrivalAndProjectBookingAndArrivalAgain_thenWarning() {
        final JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(1, 8, JourneyDirection.BACK, WorkingLocation.MAIN);
        final ProjectBooking projectTimeEntryTwo = projectTimeEntryFor(8, 10);
        final JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(10, 12, JourneyDirection.BACK, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntryOne, projectTimeEntryTwo, journeyTimeEntryThree));

        // TODO: Both journey entries cause TO_MISSING Warning, because all are checked separately
        assertThat(warnings).hasSize(2);
        assertThat(warnings.getFirst().type()).isNotNull();
        assertThat(warnings.getFirst().type()).isEqualTo(WorkTimeWarningType.TO_MISSING);
        assertThat(warnings.get(1).type()).isEqualTo(WorkTimeWarningType.TO_MISSING);
    }

    @Test
    void whenArrivalAndProjectBookingAndFurther_thenWarning() {
        final JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(1, 8, JourneyDirection.BACK, WorkingLocation.MAIN);
        final ProjectBooking projectTimeEntryTwo = projectTimeEntryFor(8, 10);
        final JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(10, 12, JourneyDirection.FURTHER, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntryOne, projectTimeEntryTwo, journeyTimeEntryThree));

        // TODO: Both journey entries cause TO_MISSING Warning, because all are checked separately
        assertThat(warnings).hasSize(2);
        assertThat(warnings.getFirst().type()).isNotNull();
        assertThat(warnings.getFirst().type()).isEqualTo(WorkTimeWarningType.TO_MISSING);
        assertThat(warnings.get(1).type()).isEqualTo(WorkTimeWarningType.TO_MISSING);
    }

    @Test
    void whenDepartureAndProjectTimeAndArrival_thenNoWarning() {
        final JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(1, 8, JourneyDirection.TO, WorkingLocation.MAIN);
        final ProjectBooking projectTimeEntryTwo = projectTimeEntryFor(8, 14);
        final JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(14, 16, JourneyDirection.BACK, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntryOne, projectTimeEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).isEmpty();
    }

    @Test
    void whenDepartureAndArrival_thenNoWarning() {
        final JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(1, 8, JourneyDirection.TO, WorkingLocation.MAIN);
        final JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(14, 16, JourneyDirection.BACK, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntryOne, journeyTimeEntryThree));

        assertThat(warnings).isEmpty();
    }

    @Test
    void whenDepartureAndProjectBookingAndFurtherAndProjectBookingAndArrival_thenNoWarning() {
        final JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(8, 9, JourneyDirection.TO, WorkingLocation.MAIN);
        final ProjectBooking projectTimeEntryTwo = projectTimeEntryFor(9, 10);
        final JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(10, 11, JourneyDirection.FURTHER, WorkingLocation.MAIN);
        final ProjectBooking projectTimeEntryFour = projectTimeEntryFor(11, 12);
        final JourneyBooking journeyTimeEntryFive = journeyTimeEntryFor(12, 13, JourneyDirection.BACK, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator
                .calculate(bookings(journeyTimeEntryOne, projectTimeEntryTwo, journeyTimeEntryThree, projectTimeEntryFour, journeyTimeEntryFive));

        assertThat(warnings).isEmpty();
    }
}
