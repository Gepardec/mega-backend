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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static org.assertj.core.api.Assertions.assertThat;

class InsufficientBreakCalculatorTest {

    private InsufficientBreakCalculator calculator;

    @BeforeEach
    void beforeEach() {
        calculator = new InsufficientBreakCalculator();
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

    private JourneyBooking journeyTimeEntryFor(final int startHour, final int startMinute, final int endHour, final int endMinute) {
        return WarningTestBookingBuilder.journeyBookingBuilder()
                .fromTime(LocalDateTime.of(2020, 1, 7, startHour, startMinute))
                .toTime(LocalDateTime.of(2020, 1, 7, endHour, endMinute))
                .task(Task.REISEN)
                .workingLocation(WorkingLocation.MAIN)
                .journeyDirection(JourneyDirection.TO)
                .vehicle(Vehicle.OTHER_INACTIVE)
                .build();
    }

    private JourneyBooking journeyTimeEntryFor(int startHour, int endHour, Vehicle vehicle) {
        return journeyTimeEntryFor(startHour, 0, endHour, 0, vehicle);
    }

    private JourneyBooking journeyTimeEntryFor(int startHour, int startMinute, int endHour, int endMinute, Vehicle vehicle) {
        return WarningTestBookingBuilder.journeyBookingBuilder()
                .fromTime(LocalDateTime.of(2020, 1, 7, startHour, startMinute))
                .toTime(LocalDateTime.of(2020, 1, 7, endHour, endMinute))
                .task(Task.REISEN)
                .workingLocation(WorkingLocation.MAIN)
                .journeyDirection(JourneyDirection.TO)
                .vehicle(vehicle)
                .build();
    }

    @Test
    void when6Hours_thenNoWarning() {
        final ProjectBooking timeEntry = projectTimeEntryFor(7, 13);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntry));

        assertThat(warnings).isEmpty();
    }

    @Test
    void when3And6HoursAnd30MinBreakTime_thenNoWarning() {
        final ProjectBooking timeEntryOne = projectTimeEntryFor(7, 10);
        final ProjectBooking timeEntryTwo = projectTimeEntryFor(10, 30, 16, 0);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo));

        assertThat(warnings).isEmpty();
    }

    @Test
    void when3And6HoursAnd1HBreakTimeActiveJourney_thenNoWarning() {
        final JourneyBooking journeyEntryOne = journeyTimeEntryFor(7, 10, Vehicle.CAR_ACTIVE);
        final JourneyBooking journeyEntryTwo = journeyTimeEntryFor(11, 17, Vehicle.CAR_ACTIVE);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyEntryOne, journeyEntryTwo));

        assertThat(warnings).isEmpty();
    }

    @Test
    void when3And6HoursAndNoBreakTimeActiveJourney_thenWarning() {
        final JourneyBooking journeyEntryOne = journeyTimeEntryFor(7, 10, Vehicle.CAR_ACTIVE);
        final JourneyBooking journeyEntryTwo = journeyTimeEntryFor(10, 16, Vehicle.CAR_ACTIVE);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyEntryOne, journeyEntryTwo));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(0.5);
    }

    @Test
    void when3EntriesAndTwo30MinutesBreak_thenNoWarning() {
        final ProjectBooking timeEntryOne = projectTimeEntryFor(7, 10);
        final ProjectBooking timeEntryTwo = projectTimeEntryFor(10, 15, 12, 15);
        final ProjectBooking timeEntryThree = projectTimeEntryFor(12, 30, 15, 30);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo, timeEntryThree));

        assertThat(warnings).isEmpty();
    }

    @Test
    void when4EntriesAnd30MinBreakAndWithOneJourneyBooking_thenWarningButIgnoredJourneyBooking() {
        final ProjectBooking threeHours = projectTimeEntryFor(7, 10);
        final ProjectBooking sixHoursFortyFiveMinutes = projectTimeEntryFor(10, 30, 17, 15);
        final JourneyBooking journey = journeyTimeEntryFor(17, 15, 19, 30);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(threeHours, sixHoursFortyFiveMinutes, journey));

        assertThat(warnings).isEmpty();
    }

    @Test
    void whenDataListEmpty_thenNoWarningsCreated() {
        assertThat(calculator.calculate(bookings())).isEmpty();
    }

    @Test
    void whenWarning_thenOnlyMissingBreakTimeSet() {
        final ProjectBooking timeEntry = projectTimeEntryFor(7, 14);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntry));

        assertThat(warnings).hasSize(1);
        final WorkTimeWarning warning = warnings.getFirst();
        assertThat(warning.date()).isNotNull();
        assertThat(warning.type()).isEqualTo(WorkTimeWarningType.MISSING_BREAK_TIME);
        assertThat(warning.hours()).isEqualTo(0.5d);
    }

    @Test
    void whenUnordered_thenOrdered() {
        final ProjectBooking timeEntryOne = projectTimeEntryFor(7, 10);
        final ProjectBooking timeEntryTwo = projectTimeEntryFor(10, 16);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryTwo, timeEntryOne));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(0.5);
    }

    @Test
    void when3And6HoursAndNoBreakTime_thenWarning() {
        final ProjectBooking timeEntryOne = projectTimeEntryFor(7, 10);
        final ProjectBooking timeEntryTwo = projectTimeEntryFor(10, 16);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(0.5);
    }

    @Test
    void when3And6HoursAnd15MinBreakTime_thenWarning() {
        final ProjectBooking timeEntryOne = projectTimeEntryFor(7, 10);
        final ProjectBooking timeEntryTwo = projectTimeEntryFor(10, 15, 16, 15);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(0.25);
    }

    @Test
    void when3EntriesAndTwo20MinutesBreak_thenWarning() {
        final ProjectBooking timeEntryOne = projectTimeEntryFor(7, 10);
        final ProjectBooking timeEntryTwo = projectTimeEntryFor(10, 10, 12, 10);
        final ProjectBooking timeEntryThree = projectTimeEntryFor(12, 20, 15, 20);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo, timeEntryThree));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(0.16);
    }

    @Test
    void when4EntriesAnd15MinBreakAndWithOneJourneyBooking_thenWarningButIgnoredJourneyBooking() {
        final ProjectBooking timeEntryOne = projectTimeEntryFor(7, 10);
        final ProjectBooking timeEntryTwo = projectTimeEntryFor(10, 15, 16, 45);
        final JourneyBooking timeEntryThree = journeyTimeEntryFor(16, 45, 19, 15);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo, timeEntryThree));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(0.25);
    }

    @Test
    @DisplayName("Tests for false positives which have been observed before a fix was introduced")
    void calculate_whenOverlappingEntriesWithBreaks_thenNoBreakWarnings() {
        final ProjectBooking timeEntryOne = projectTimeEntryFor(8, 0, 11, 0);
        final ProjectBooking timeEntryTwo = projectTimeEntryFor(11, 30, 13, 0);
        final ProjectBooking timeEntryThree = projectTimeEntryFor(12, 15, 16, 0);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo, timeEntryThree));

        assertThat(warnings)
                .isEmpty();
    }

    @Test
    @DisplayName("Tests for correct warnings when overlapping entries exist")
    void calculate_whenOverlappingEntriesWithoutBreaks_thenBreakWarnings() {
        final ProjectBooking timeEntryOne = projectTimeEntryFor(8, 0, 12, 0);
        final ProjectBooking timeEntryTwo = projectTimeEntryFor(11, 30, 12, 0);
        final ProjectBooking timeEntryThree = projectTimeEntryFor(12, 0, 16, 45);
        final ProjectBooking timeEntryFour = projectTimeEntryFor(12, 30, 16, 30);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo, timeEntryThree, timeEntryFour));

        assertThat(warnings)
                .hasSize(1);
        assertThat(warnings.getFirst().hours())
                .isEqualTo(0.50d);
    }

    @Test
    @DisplayName("Tests for false positives when overlapping entries exist")
    void calculate_whenOverlappingEntriesWithBreak_thenNoBreakWarnings() {
        final ProjectBooking timeEntryOne = projectTimeEntryFor(8, 0, 12, 0);
        final ProjectBooking timeEntryTwo = projectTimeEntryFor(11, 30, 12, 0);
        final ProjectBooking timeEntryThree = projectTimeEntryFor(12, 30, 16, 45);
        final ProjectBooking timeEntryFour = projectTimeEntryFor(12, 30, 16, 30);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo, timeEntryThree, timeEntryFour));

        assertThat(warnings)
                .isEmpty();
    }

    @Test
    void whenBreakTakenAfterContinuousWorkExceeds6Hours_thenWarning() {
        final ProjectBooking eightHours = projectTimeEntryFor(6, 14);
        final ProjectBooking twoHours = projectTimeEntryFor(15, 17);

        final List<WorkTimeWarning> warnings = calculator.calculate(bookings(eightHours, twoHours));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(0.5);
    }
}
