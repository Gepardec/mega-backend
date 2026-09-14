package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.Vehicle;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static org.assertj.core.api.Assertions.assertThat;

class CoreWorkingHoursCalculatorTest {

    private CoreWorkingHoursCalculator calculator;

    @BeforeEach
    void init() {
        calculator = new CoreWorkingHoursCalculator();
    }

    private static ProjectBooking projectTimeEntryFor(int startHour, int endHour) {
        return projectTimeEntryFor(startHour, 0, endHour, 0);
    }

    private static ProjectBooking projectTimeEntryFor(int startHour, int startMinute, int endHour, int endMinute) {
        return WarningTestBookingBuilder.projectBookingBuilder()
                .fromTime(LocalDateTime.of(2020, 1, 7, startHour, startMinute))
                .toTime(LocalDateTime.of(2020, 1, 7, endHour, endMinute))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN).build();
    }

    private static JourneyBooking journeyTimeEntryFor(int startHour, int endHour, Vehicle vehicle) {
        return journeyTimeEntryFor(startHour, endHour, 0, vehicle);
    }

    private static JourneyBooking journeyTimeEntryFor(int startHour, int endHour, int endMinute, Vehicle vehicle) {
        return WarningTestBookingBuilder.journeyBookingBuilder()
                .fromTime(LocalDateTime.of(2020, 1, 7, startHour, 0))
                .toTime(LocalDateTime.of(2020, 1, 7, endHour, endMinute))
                .task(Task.REISEN)
                .workingLocation(WorkingLocation.MAIN)
                .journeyDirection(JourneyDirection.TO)
                .vehicle(vehicle)
                .build();
    }

    @Test
    void whenUnordered_thenNoWarning() {
        JourneyBooking travelBefore = journeyTimeEntryFor(1, 6, Vehicle.OTHER_INACTIVE);
        ProjectBooking start = projectTimeEntryFor(6, 12);
        ProjectBooking end = projectTimeEntryFor(13, 22);
        JourneyBooking travelAfter = journeyTimeEntryFor(22, 23, Vehicle.OTHER_INACTIVE);

        List<WorkTimeWarning> result = calculator.calculate(bookings(travelAfter, end, start, travelBefore));

        assertThat(result).isEmpty();
    }

    @Test
    void whenWarning_thenWorkTimeWarningTypeSet() {
        ProjectBooking start = projectTimeEntryFor(5, 12);
        ProjectBooking end = projectTimeEntryFor(13, 16);

        List<WorkTimeWarning> result = calculator.calculate(bookings(start, end));

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().type()).isNotNull();
        assertThat(result.getFirst().type()).isEqualTo(WorkTimeWarningType.OUTSIDE_CORE_WORKING_TIME);
    }

    private static Stream<Arguments> provideTimeEntries() {
        return Stream.of(
                Arguments.of(
                        List.of(
                                projectTimeEntryFor(5, 12),
                                projectTimeEntryFor(13, 16)
                        ),
                        1
                ),
                Arguments.of(
                        List.of(
                                journeyTimeEntryFor(5, 12, Vehicle.CAR_ACTIVE),
                                journeyTimeEntryFor(13, 16, Vehicle.CAR_ACTIVE)
                        ),
                        1
                ),
                Arguments.of(
                        List.of(
                                projectTimeEntryFor(5, 12),
                                projectTimeEntryFor(18, 23)
                        ),
                        1
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideTimeEntries")
    void whenStartedTooEarly_thenWarning(List<WorkTimeBooking> entries, int expectedWarnings) {
        List<WorkTimeWarning> result = calculator.calculate(new WorkTimeBookings(entries));

        assertThat(result).hasSize(expectedWarnings);
    }

    @Test
    void whenInactiveTravelerOnJourneyAndStartedToEarly_thenWarning() {
        JourneyBooking start = journeyTimeEntryFor(3, 4, Vehicle.OTHER_INACTIVE);
        ProjectBooking end = projectTimeEntryFor(5, 8);

        List<WorkTimeWarning> result = calculator.calculate(bookings(start, end));

        assertThat(result).hasSize(1);
    }

    @Test
    void whenActiveTravelerOnJourneyAndStartedToEarly_thenWarning() {
        JourneyBooking start = journeyTimeEntryFor(3, 6, Vehicle.CAR_ACTIVE);

        List<WorkTimeWarning> result = calculator.calculate(bookings(start));

        assertThat(result).hasSize(1);
    }

    @Test
    void whenDataListEmpty_thenNoWarnings() {
        assertThat(calculator.calculate(bookings())).isEmpty();
    }

    @Test
    void whenStoppedToLateAt23_thenWarning() {
        ProjectBooking start = projectTimeEntryFor(6, 12);
        ProjectBooking end = projectTimeEntryFor(18, 23);

        List<WorkTimeWarning> result = calculator.calculate(bookings(start, end));

        assertThat(result).hasSize(1);
    }

    @Test
    void whenValid_thenNoWarning() {
        JourneyBooking travelInactiveBefore = journeyTimeEntryFor(1, 6, Vehicle.OTHER_INACTIVE);
        JourneyBooking travelActiveBefore = journeyTimeEntryFor(6, 10, Vehicle.CAR_ACTIVE);
        ProjectBooking start = projectTimeEntryFor(10, 12);
        ProjectBooking end = projectTimeEntryFor(13, 16);
        JourneyBooking travelActiveAfter = journeyTimeEntryFor(17, 22, Vehicle.CAR_ACTIVE);
        JourneyBooking travelInactiveAfter = journeyTimeEntryFor(22, 23, Vehicle.OTHER_INACTIVE);

        List<WorkTimeWarning> result = calculator
                .calculate(bookings(travelInactiveBefore, travelActiveBefore, start, end, travelActiveAfter, travelInactiveAfter));

        assertThat(result).isEmpty();
    }

    @Test
    void whenOnlyInactiveTravelerOnJourney_thenNoWarning() {
        JourneyBooking start = journeyTimeEntryFor(3, 10, Vehicle.OTHER_INACTIVE);
        JourneyBooking end = journeyTimeEntryFor(10, 23, Vehicle.OTHER_INACTIVE);

        List<WorkTimeWarning> result = calculator.calculate(bookings(start, end));

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_JourneyDurationZeroOutsideCoreWorkingTime_NoWarning() {
        // Given
        var entry = journeyTimeEntryFor(5, 5, Vehicle.CAR_ACTIVE);

        // When
        var result = calculator.calculate(bookings(entry));

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void calculate_JourneyDurationZeroInsideCoreWorkingTime_NoWarning() {
        // Given
        var entry = journeyTimeEntryFor(11, 11, Vehicle.CAR_ACTIVE);

        // When
        var result = calculator.calculate(bookings(entry));

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void calculate_JourneyDurationNotZeroOutsideCoreWorkingTime_Warning() {
        // Given
        var entry = journeyTimeEntryFor(3, 3, 30, Vehicle.CAR_ACTIVE);

        // When
        var result = calculator.calculate(bookings(entry));

        // Then
        assertThat(result).hasSize(1);
    }
}
