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
import java.util.ArrayList;
import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static org.assertj.core.api.Assertions.assertThat;

class InvalidWorkingLocationInJourneyCalculatorTest {

    private InvalidWorkingLocationInJourneyCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new InvalidWorkingLocationInJourneyCalculator();
    }

    @Test
    void getAllWarningsForEmployeeAndMonth_whenInvalidJourneyWorkingLocation_thenGetError() {
        List<WorkTimeBooking> projectEntries = createWorkTimeBookingListForRequestForJourney();

        List<WorkTimeWarning> actual = calculator.calculate(new com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings(projectEntries));

        assertThat(actual)
                .hasSize(1)
                .extracting(WorkTimeWarning::type)
                .containsExactly(WorkTimeWarningType.INVALID_WORKING_LOCATION);
    }

    private List<WorkTimeBooking> createWorkTimeBookingListForRequestForJourney() {
        List<WorkTimeBooking> projectEntries = new ArrayList<>();

        //departure day
        projectEntries.add(createProjectBooking(
                LocalDateTime.of(2025, 7, 1, 8, 0),
                LocalDateTime.of(2025, 7, 1, 9, 45),
                Task.BEARBEITEN, WorkingLocation.MAIN, "1033"));

        projectEntries.add(createJourneyBooking(
                LocalDateTime.of(2025, 7, 1, 10, 15),
                LocalDateTime.of(2025, 7, 1, 10, 45),
                WorkingLocation.A, JourneyDirection.TO));

        projectEntries.add(createProjectBooking(
                LocalDateTime.of(2025, 7, 1, 11, 0),
                LocalDateTime.of(2025, 7, 1, 11, 30),
                Task.BEARBEITEN, WorkingLocation.A, "1033"));

        projectEntries.add(createProjectBooking(
                LocalDateTime.of(2025, 7, 1, 12, 0),
                LocalDateTime.of(2025, 7, 1, 16, 30),
                Task.BEARBEITEN, WorkingLocation.A, "1033"));

        //Tag1 Away
        projectEntries.add(createProjectBooking(
                LocalDateTime.of(2025, 7, 2, 8, 0),
                LocalDateTime.of(2025, 7, 1, 11, 30),
                Task.BEARBEITEN, WorkingLocation.MAIN, "1033"));

        projectEntries.add(createProjectBooking(
                LocalDateTime.of(2025, 7, 2, 12, 0),
                LocalDateTime.of(2025, 7, 1, 16, 30),
                Task.BEARBEITEN, WorkingLocation.A, "1033"));

        //Tag2 Away
        projectEntries.add(createProjectBooking(
                LocalDateTime.of(2025, 7, 3, 8, 0),
                LocalDateTime.of(2025, 7, 1, 11, 30),
                Task.BEARBEITEN, WorkingLocation.A, "1033"));

        projectEntries.add(createProjectBooking(
                LocalDateTime.of(2025, 7, 3, 12, 0),
                LocalDateTime.of(2025, 7, 1, 16, 30),
                Task.BEARBEITEN, WorkingLocation.A, "1033"));

        //arrival day
        projectEntries.add(createProjectBooking(
                LocalDateTime.of(2025, 7, 4, 8, 0),
                LocalDateTime.of(2025, 7, 1, 11, 30),
                Task.BEARBEITEN, WorkingLocation.A, "1033"));

        projectEntries.add(createJourneyBooking(
                LocalDateTime.of(2025, 7, 4, 12, 15),
                LocalDateTime.of(2025, 7, 1, 12, 45),
                WorkingLocation.MAIN, JourneyDirection.BACK));

        projectEntries.add(createProjectBooking(
                LocalDateTime.of(2025, 7, 4, 12, 30),
                LocalDateTime.of(2025, 7, 1, 16, 30),
                Task.BEARBEITEN, WorkingLocation.MAIN, "1033"));

        return projectEntries;
    }

    private WorkTimeBooking createProjectBooking(LocalDateTime from, LocalDateTime to, Task task, WorkingLocation location, String processId) {
        return WarningTestBookingBuilder.projectBookingBuilder()
                .fromTime(from)
                .toTime(to)
                .task(task)
                .workingLocation(location)
                .process(processId)
                .build();
    }

    private JourneyBooking createJourneyBooking(LocalDateTime from, LocalDateTime to, WorkingLocation workLoc, JourneyDirection direction) {
        return WarningTestBookingBuilder.journeyBookingBuilder()
                .fromTime(from)
                .toTime(to)
                .task(Task.REISEN)
                .workingLocation(workLoc)
                .journeyDirection(direction)
                .vehicle(Vehicle.OTHER_INACTIVE)
                .build();
    }

    private ProjectBooking projectTimeEntryFor(int startHour, int endHour, WorkingLocation workingLocation) {
        return projectTimeEntryFor(startHour, endHour, 0, workingLocation);
    }

    private ProjectBooking projectTimeEntryFor(int startHour, int endHour, int endMinute, WorkingLocation workingLocation) {

        return WarningTestBookingBuilder.projectBookingBuilder()
                .fromTime(LocalDateTime.of(2020, 1, 7, startHour, 0))
                .toTime(LocalDateTime.of(2020, 1, 7, endHour, endMinute))
                .task(Task.BEARBEITEN)
                .workingLocation(workingLocation)
                .build();
    }

    private JourneyBooking journeyTimeEntryFor(int startHour, int endHour, JourneyDirection direction, WorkingLocation workingLocation) {
        return journeyTimeEntryFor(startHour, endHour, 0, direction, workingLocation);
    }

    private JourneyBooking journeyTimeEntryFor(int startHour, int endHour, int endMinute, JourneyDirection direction, WorkingLocation workingLocation) {
        return WarningTestBookingBuilder.journeyBookingBuilder()
                .fromTime(LocalDateTime.of(2020, 1, 7, startHour, 0))
                .toTime(LocalDateTime.of(2020, 1, 7, endHour, endMinute))
                .task(Task.REISEN)
                .workingLocation(workingLocation)
                .journeyDirection(direction)
                .vehicle(Vehicle.OTHER_INACTIVE)
                .build();
    }

    @Test
    void whenOneProjectBookingWithinJourneyWithWorkingLocationMain_thenWarning() {
        JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO, WorkingLocation.A);
        WorkTimeBooking projectEntryTwo = projectTimeEntryFor(8, 9, WorkingLocation.OTHER);
        JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(9, 10, JourneyDirection.BACK, WorkingLocation.A);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntryOne, projectEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().type()).isNotNull();
        assertThat(warnings.getFirst().type()).isEqualTo(WorkTimeWarningType.INVALID_WORKING_LOCATION);
    }

    @Test
    void whenTwoProjectBookingWithinJourneyWithOneInvalidWorkingLocation_thenOneWarning() {
        JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO, WorkingLocation.A);
        WorkTimeBooking projectEntryTwo = projectTimeEntryFor(8, 9, WorkingLocation.A);
        WorkTimeBooking projectEntryThree = projectTimeEntryFor(9, 10, WorkingLocation.MAIN);
        JourneyBooking journeyTimeEntryFour = journeyTimeEntryFor(10, 11, JourneyDirection.BACK, WorkingLocation.A);

        List<WorkTimeWarning> warnings = calculator
                .calculate(bookings(journeyTimeEntryOne, projectEntryTwo, projectEntryThree, journeyTimeEntryFour));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().type()).isNotNull();
        assertThat(warnings.getFirst().type()).isEqualTo(WorkTimeWarningType.INVALID_WORKING_LOCATION);
    }

    @Test
    void whenTwoProjectBookingWithinTwoJourneysWithOneInvalidWorkingLocation_thenOneWarning() {
        JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO, WorkingLocation.A);
        WorkTimeBooking projectEntryTwo = projectTimeEntryFor(8, 9, WorkingLocation.OTHER);
        JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(9, 10, JourneyDirection.BACK, WorkingLocation.A);
        JourneyBooking journeyTimeEntryFour = journeyTimeEntryFor(10, 11, JourneyDirection.TO, WorkingLocation.OTHER);
        WorkTimeBooking projectEntryFive = projectTimeEntryFor(11, 12, WorkingLocation.OTHER);
        JourneyBooking journeyTimeEntrySix = journeyTimeEntryFor(12, 13, JourneyDirection.BACK, WorkingLocation.A);

        List<WorkTimeWarning> warnings = calculator
                .calculate(bookings(journeyTimeEntryOne, projectEntryTwo, journeyTimeEntryThree, journeyTimeEntryFour, projectEntryFive,
                        journeyTimeEntrySix));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().type()).isNotNull();
        assertThat(warnings.getFirst().type()).isEqualTo(WorkTimeWarningType.INVALID_WORKING_LOCATION);
    }

    @Test
    void whenProjectBookingWithWorkingLocationMAINAfterJourneyBackWithWorkingLocationA_thenNoWarning() {
        JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(8, 10, JourneyDirection.TO, WorkingLocation.A);
        ProjectBooking projectTimeEntryTwo = projectTimeEntryFor(10, 11, WorkingLocation.A);
        JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(12, 13, JourneyDirection.BACK, WorkingLocation.A);
        ProjectBooking projectTimeEntryFour = projectTimeEntryFor(15, 16, WorkingLocation.MAIN);

        List<WorkTimeWarning> warnings = calculator
                .calculate(bookings(journeyTimeEntryOne, projectTimeEntryTwo, journeyTimeEntryThree, projectTimeEntryFour));

        assertThat(warnings).isEmpty();
    }

    @Test
    void whenProjectBookingWithWorkingLocationAAfterJourneyBackWithWorkingLocationA_thenNoWarning() {
        JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(8, 10, JourneyDirection.TO, WorkingLocation.A);
        ProjectBooking projectTimeEntryTwo = projectTimeEntryFor(10, 11, WorkingLocation.A);
        JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(12, 13, JourneyDirection.BACK, WorkingLocation.A);
        ProjectBooking projectTimeEntryFour = projectTimeEntryFor(15, 16, WorkingLocation.A);

        List<WorkTimeWarning> warnings = calculator
                .calculate(bookings(journeyTimeEntryOne, projectTimeEntryTwo, journeyTimeEntryThree, projectTimeEntryFour));

        assertThat(warnings).hasSize(1);
    }

    @Test
    void whenOneProjectBookingWithinJourney_thenNoWarning() {
        JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO, WorkingLocation.A);
        WorkTimeBooking projectEntryTwo = projectTimeEntryFor(8, 9, WorkingLocation.A);
        JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(9, 10, JourneyDirection.BACK, WorkingLocation.A);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyTimeEntryOne, projectEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).isEmpty();
    }

    @Test
    void whenTwoProjectBookingWithinJourney_thenNoWarning() {
        JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO, WorkingLocation.A);
        WorkTimeBooking projectEntryTwo = projectTimeEntryFor(8, 9, WorkingLocation.A);
        WorkTimeBooking projectEntryThree = projectTimeEntryFor(9, 10, WorkingLocation.A);
        JourneyBooking journeyTimeEntryFour = journeyTimeEntryFor(10, 11, JourneyDirection.BACK, WorkingLocation.A);

        List<WorkTimeWarning> warnings = calculator
                .calculate(bookings(journeyTimeEntryOne, projectEntryTwo, projectEntryThree, journeyTimeEntryFour));

        assertThat(warnings).isEmpty();
    }

    @Test
    void whenTwoProjectBookingWithinTwoJourneys_thenNoWarning() {
        JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO, WorkingLocation.A);
        WorkTimeBooking projectEntryTwo = projectTimeEntryFor(8, 9, WorkingLocation.A);
        JourneyBooking journeyTimeEntryThree = journeyTimeEntryFor(9, 10, JourneyDirection.BACK, WorkingLocation.A);
        JourneyBooking journeyTimeEntryFour = journeyTimeEntryFor(10, 11, JourneyDirection.TO, WorkingLocation.OTHER);
        WorkTimeBooking projectEntryFive = projectTimeEntryFor(11, 12, WorkingLocation.OTHER);
        JourneyBooking journeyTimeEntrySix = journeyTimeEntryFor(12, 13, JourneyDirection.BACK, WorkingLocation.A);

        List<WorkTimeWarning> warnings = calculator
                .calculate(bookings(journeyTimeEntryOne, projectEntryTwo, journeyTimeEntryThree, journeyTimeEntryFour, projectEntryFive,
                        journeyTimeEntrySix));

        assertThat(warnings).isEmpty();
    }

    @Test
    void whenWorkTimeBookingAndJourneyEntryHaveSameStartHourSort_thenNoWarning() {
        JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(7, 7, JourneyDirection.TO, WorkingLocation.A);
        WorkTimeBooking projectEntryOne = projectTimeEntryFor(7, 8, WorkingLocation.A);
        WorkTimeBooking projectEntryTwo = projectTimeEntryFor(11, 12, WorkingLocation.A);
        JourneyBooking journeyTimeEntryTwo = journeyTimeEntryFor(12, 13, JourneyDirection.BACK, WorkingLocation.A);

        List<WorkTimeWarning> warnings = calculator
                .calculate(bookings(journeyTimeEntryOne, projectEntryOne, projectEntryTwo, journeyTimeEntryTwo));
        assertThat(warnings).isEmpty();
    }

    @Test
    void whenWorkTimeBookingAndJourneyEntryHaveSameStartHourSortReversed_thenNoWarning() {
        JourneyBooking journeyTimeEntryOne = journeyTimeEntryFor(7, 7, JourneyDirection.TO, WorkingLocation.A);
        WorkTimeBooking projectEntryOne = projectTimeEntryFor(7, 8, WorkingLocation.A);
        WorkTimeBooking projectEntryTwo = projectTimeEntryFor(11, 12, WorkingLocation.A);
        JourneyBooking journeyTimeEntryTwo = journeyTimeEntryFor(12, 13, JourneyDirection.BACK, WorkingLocation.A);

        List<WorkTimeWarning> warnings = calculator
                .calculate(bookings(projectEntryOne, journeyTimeEntryOne, journeyTimeEntryTwo, projectEntryTwo));
        assertThat(warnings).isEmpty();
    }
}
