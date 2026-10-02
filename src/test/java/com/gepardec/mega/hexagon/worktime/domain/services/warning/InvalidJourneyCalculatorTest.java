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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static org.assertj.core.api.Assertions.assertThat;

class InvalidJourneyCalculatorTest {

    private static final LocalDate DAY = LocalDate.of(2020, 1, 7);
    private static final LocalDate NEXT_DAY = DAY.plusDays(1);

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
        return journeyTimeEntryFor(DAY, startHour, endHour, direction, workingLocation);
    }

    private JourneyBooking journeyTimeEntryFor(final LocalDate day, final int startHour, final int endHour,
                                               final JourneyDirection direction, final WorkingLocation workingLocation) {
        return WarningTestBookingBuilder.journeyBookingBuilder()
                .fromTime(day.atTime(startHour, 0))
                .toTime(day.atTime(endHour, 0))
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
    void whenFurtherAndProjectBookingAndArrival_thenWarningForEachJourney() {
        final JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(1, 8, JourneyDirection.FURTHER, WorkingLocation.MAIN);
        final ProjectBooking projectTimeEntryTwo = projectTimeEntryFor(8, 10);
        final JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(NEXT_DAY, 10, 12, JourneyDirection.BACK, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntryOne, projectTimeEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).containsExactly(
                new WorkTimeWarning(DAY, WorkTimeWarningType.TO_MISSING, null),
                new WorkTimeWarning(NEXT_DAY, WorkTimeWarningType.TO_MISSING, null));
    }

    @Test
    void whenDepartureAndProjectBookingAndDepartureAgain_thenWarningForEachOpenTrip() {
        final JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(1, 8, JourneyDirection.TO, WorkingLocation.MAIN);
        final ProjectBooking projectTimeEntryTwo = projectTimeEntryFor(8, 10);
        final JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(NEXT_DAY, 10, 12, JourneyDirection.TO, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntryOne, projectTimeEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).containsExactly(
                new WorkTimeWarning(DAY, WorkTimeWarningType.BACK_MISSING, null),
                new WorkTimeWarning(NEXT_DAY, WorkTimeWarningType.BACK_MISSING, null));
    }

    @Test
    void whenArrivalAndProjectBookingAndArrivalAgain_thenWarningForEachJourney() {
        final JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(1, 8, JourneyDirection.BACK, WorkingLocation.MAIN);
        final ProjectBooking projectTimeEntryTwo = projectTimeEntryFor(8, 10);
        final JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(NEXT_DAY, 10, 12, JourneyDirection.BACK, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntryOne, projectTimeEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).containsExactly(
                new WorkTimeWarning(DAY, WorkTimeWarningType.TO_MISSING, null),
                new WorkTimeWarning(NEXT_DAY, WorkTimeWarningType.TO_MISSING, null));
    }

    @Test
    void whenArrivalAndProjectBookingAndFurther_thenWarningForEachJourney() {
        final JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(1, 8, JourneyDirection.BACK, WorkingLocation.MAIN);
        final ProjectBooking projectTimeEntryTwo = projectTimeEntryFor(8, 10);
        final JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(NEXT_DAY, 10, 12, JourneyDirection.FURTHER, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntryOne, projectTimeEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).containsExactly(
                new WorkTimeWarning(DAY, WorkTimeWarningType.TO_MISSING, null),
                new WorkTimeWarning(NEXT_DAY, WorkTimeWarningType.TO_MISSING, null));
    }

    @Test
    void whenTripWithFurtherIsInterruptedByNewDeparture_thenWarningOnDateOfTripsLastJourney() {
        final JourneyBooking departure = journeyTimeEntryFor(DAY, 8, 9, JourneyDirection.TO, WorkingLocation.A);
        final JourneyBooking further = journeyTimeEntryFor(NEXT_DAY, 8, 9, JourneyDirection.FURTHER, WorkingLocation.A);
        final JourneyBooking newDeparture = journeyTimeEntryFor(NEXT_DAY.plusDays(1), 8, 9, JourneyDirection.TO, WorkingLocation.A);
        final JourneyBooking arrival = journeyTimeEntryFor(NEXT_DAY.plusDays(2), 8, 9, JourneyDirection.BACK, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(departure, further, newDeparture, arrival));

        assertThat(warnings).containsExactly(new WorkTimeWarning(NEXT_DAY, WorkTimeWarningType.BACK_MISSING, null));
    }

    @Test
    void whenFurtherAndArrivalFollowCompletedTrip_thenWarningForEachJourney() {
        final JourneyBooking departure = journeyTimeEntryFor(DAY, 8, 9, JourneyDirection.TO, WorkingLocation.A);
        final JourneyBooking arrival = journeyTimeEntryFor(NEXT_DAY, 8, 9, JourneyDirection.BACK, WorkingLocation.MAIN);
        final JourneyBooking further = journeyTimeEntryFor(NEXT_DAY.plusDays(1), 8, 9, JourneyDirection.FURTHER, WorkingLocation.A);
        final JourneyBooking secondArrival = journeyTimeEntryFor(NEXT_DAY.plusDays(2), 8, 9, JourneyDirection.BACK, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(departure, arrival, further, secondArrival));

        assertThat(warnings).containsExactly(
                new WorkTimeWarning(NEXT_DAY.plusDays(1), WorkTimeWarningType.TO_MISSING, null),
                new WorkTimeWarning(NEXT_DAY.plusDays(2), WorkTimeWarningType.TO_MISSING, null));
    }

    @Test
    void whenFurtherAfterCompletedTripIsFollowedByCompleteTrip_thenOnlyFurtherIsFlagged() {
        final JourneyBooking departure = journeyTimeEntryFor(DAY, 8, 9, JourneyDirection.TO, WorkingLocation.A);
        final JourneyBooking arrival = journeyTimeEntryFor(NEXT_DAY, 8, 9, JourneyDirection.BACK, WorkingLocation.MAIN);
        final JourneyBooking further = journeyTimeEntryFor(NEXT_DAY.plusDays(1), 8, 9, JourneyDirection.FURTHER, WorkingLocation.A);
        final JourneyBooking secondDeparture = journeyTimeEntryFor(NEXT_DAY.plusDays(2), 8, 9, JourneyDirection.TO, WorkingLocation.A);
        final JourneyBooking secondArrival = journeyTimeEntryFor(NEXT_DAY.plusDays(3), 8, 9, JourneyDirection.BACK, WorkingLocation.MAIN);

        final List<WorkTimeWarning> warnings = calculator.calculate(
                bookings(departure, arrival, further, secondDeparture, secondArrival));

        assertThat(warnings).containsExactly(new WorkTimeWarning(NEXT_DAY.plusDays(1), WorkTimeWarningType.TO_MISSING, null));
    }

    @Test
    void whenTripCrossesMonthBoundary_thenBackMissingInEarlierMonthAndToMissingInLaterMonth() {
        final LocalDate lastDayOfMonth = LocalDate.of(2020, 1, 31);
        final LocalDate firstDayOfNextMonth = LocalDate.of(2020, 2, 1);
        final JourneyBooking departure = journeyTimeEntryFor(lastDayOfMonth, 16, 18, JourneyDirection.TO, WorkingLocation.A);
        final JourneyBooking arrival = journeyTimeEntryFor(firstDayOfNextMonth, 16, 18, JourneyDirection.BACK, WorkingLocation.MAIN);

        final List<WorkTimeWarning> earlierMonth = calculator.calculate(bookings(departure));
        final List<WorkTimeWarning> laterMonth = calculator.calculate(bookings(arrival));

        assertThat(earlierMonth).containsExactly(new WorkTimeWarning(lastDayOfMonth, WorkTimeWarningType.BACK_MISSING, null));
        assertThat(laterMonth).containsExactly(new WorkTimeWarning(firstDayOfNextMonth, WorkTimeWarningType.TO_MISSING, null));
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
