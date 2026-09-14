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

class ExceededMaximumWorkingHoursPerDayCalculatorTest {

    private ExceededMaximumWorkingHoursPerDayCalculator calculator;

    @BeforeEach
    void beforeEach() {
        calculator = new ExceededMaximumWorkingHoursPerDayCalculator();
    }

    private ProjectBooking projectTimeEntryFor(int startHour, int endHour) {
        return projectTimeEntryFor(startHour, 0, endHour, 0);
    }

    private ProjectBooking projectTimeEntryFor(int startHour, int startMinute, int endHour, int endMinute) {
        return WarningTestBookingBuilder.projectBookingBuilder()
                .fromTime(LocalDateTime.of(2020, 1, 7, startHour, startMinute))
                .toTime(LocalDateTime.of(2020, 1, 7, endHour, endMinute))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
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
    void when10HoursPerDay_thenNoWarning() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(7, 0, 12, 0);
        ProjectBooking timeEntryTwo = projectTimeEntryFor(13, 0, 18, 0);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo));

        assertThat(warnings).isEmpty();
    }

    @Test
    void whenInactiveJourney10HoursPerDay_thenNoWarning() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(7, 12);
        ProjectBooking timeEntryTwo = projectTimeEntryFor(13, 18);
        JourneyBooking timeEntryThree = journeyTimeEntryFor(18, 22, Vehicle.CAR_INACTIVE);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo, timeEntryThree));

        assertThat(warnings).isEmpty();
    }

    @Test
    void whenActiveJourney14HoursPerDay_thenWarning() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(7, 12);
        ProjectBooking timeEntryTwo = projectTimeEntryFor(13, 18);
        JourneyBooking timeEntryThree = journeyTimeEntryFor(18, 22, Vehicle.CAR_ACTIVE);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo, timeEntryThree));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(4);
    }

    @Test
    void whenActiveJourney12HoursPerDayUnordered_thenWarning() {
        JourneyBooking timeEntryOne = journeyTimeEntryFor(18, 22, Vehicle.CAR_ACTIVE);
        JourneyBooking timeEntryTwo = journeyTimeEntryFor(18, 22, Vehicle.CAR_ACTIVE);
        JourneyBooking timeEntryThree = journeyTimeEntryFor(18, 22, Vehicle.CAR_ACTIVE);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryTwo, timeEntryOne, timeEntryThree));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(2);
    }

    @Test
    void when10HoursPerDayOnlyInactiveJourney_thenNoWarning() {
        JourneyBooking timeEntryOne = journeyTimeEntryFor(7, 12, Vehicle.CAR_INACTIVE);
        JourneyBooking timeEntryTwo = journeyTimeEntryFor(13, 18, Vehicle.CAR_INACTIVE);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo));

        assertThat(warnings).isEmpty();
    }

    @Test
    void calculate_whenDataListEmpty_thenNoWarningsCreated() {
        assertThat(calculator.calculate(bookings())).isEmpty();
    }

    @Test
    void whenWarning_thenOnlyExcessWorkTimeSet() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(7, 12);
        ProjectBooking timeEntryTwo = projectTimeEntryFor(13, 19);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo));

        assertThat(warnings).hasSize(1);
        WorkTimeWarning warning = warnings.getFirst();
        assertThat(warning.date()).isNotNull();
        assertThat(warning.type()).isEqualTo(WorkTimeWarningType.EXCESS_WORKING_TIME_PRESENT);
        assertThat(warning.hours()).isEqualTo(1d);
    }

    @Test
    void whenUnordered_thenOrdered() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(7, 12);
        ProjectBooking timeEntryTwo = projectTimeEntryFor(13, 19);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryTwo, timeEntryOne));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(1);
    }

    @Test
    void when11HoursPerDay_thenWarning() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(7, 12);
        ProjectBooking timeEntryTwo = projectTimeEntryFor(13, 19);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(1);
    }

    @Test
    void whenOneJourneyEntry11HoursPerDay_thenWarning() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(7, 12);
        ProjectBooking timeEntryTwo = projectTimeEntryFor(13, 19);
        JourneyBooking timeEntryThree = journeyTimeEntryFor(19, 22, Vehicle.CAR_INACTIVE);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo, timeEntryThree));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(1);
    }
}
