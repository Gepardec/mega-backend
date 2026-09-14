package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.Vehicle;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static org.assertj.core.api.Assertions.assertThat;

class InsufficientRestCalculatorTest {

    private final InsufficientRestCalculator calculator = new InsufficientRestCalculator();

    @Test
    void calculate_whenDataListEmpty_thenNoWarningsCreated() {
        assertThat(calculator.calculate(bookings())).isEmpty();
    }

    private ProjectBooking projectTimeEntryFor(int day, int startHour, int startMinute, int endHour, int endMinute) {
        return projectTimeEntryFor(day, startHour, startMinute, day, endHour, endMinute);
    }

    private ProjectBooking projectTimeEntryFor(int startDay, int startHour, int startMinute, int endDay, int endHour, int endMinute) {
        return WarningTestBookingBuilder.projectBookingBuilder()
                .fromTime(LocalDateTime.of(2020, 1, startDay, startHour, startMinute))
                .toTime(LocalDateTime.of(2020, 1, endDay, endHour, endMinute))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
                .build();
    }

    private JourneyBooking journeyTimeEntryFor(int startDay, int startHour, int startMinute, int endDay, int endHour, int endMinute, Vehicle vehicle) {
        return WarningTestBookingBuilder.journeyBookingBuilder()
                .fromTime(LocalDateTime.of(2020, startDay, 7, startHour, startMinute))
                .toTime(LocalDateTime.of(2020, endDay, 7, endHour, endMinute))
                .task(Task.REISEN)
                .workingLocation(WorkingLocation.MAIN)
                .journeyDirection(JourneyDirection.TO)
                .vehicle(vehicle)
                .build();
    }

    @Test
    void whenDataListEmpty_thenNoWarningsCreated() {
        assertThat(calculator.calculate(bookings())).isEmpty();
    }

    @Test
    void whenWarning_thenOnlyMissingRestTimeSet() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(1, 16, 0, 22, 0);
        ProjectBooking timeEntryTwo = projectTimeEntryFor(2, 8, 0, 11, 0);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo));

        assertThat(warnings).hasSize(1);
        WorkTimeWarning warning = warnings.getFirst();
        assertThat(warning.date()).isNotNull();
        assertThat(warning.type()).isEqualTo(WorkTimeWarningType.MISSING_REST_TIME);
        assertThat(warning.hours()).isEqualTo(1d);
    }

    @Test
    void whenUnordered_thenOrdered() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(1, 16, 0, 22, 0);
        ProjectBooking timeEntryTwo = projectTimeEntryFor(2, 8, 0, 11, 0);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryTwo, timeEntryOne));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(1);
    }

    @Test
    void when10HoursRestTime_thenWarning() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(1, 16, 0, 22, 0);
        ProjectBooking timeEntryTwo = projectTimeEntryFor(2, 8, 0, 11, 0);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(1);
    }

    @Test
    void whenOneJourneyEntryAnd10HoursRestTime_thenWarning() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(1, 16, 0, 22, 0);
        JourneyBooking timeEntryTwo = journeyTimeEntryFor(1, 22, 0, 2, 1, 0, Vehicle.CAR_INACTIVE);
        ProjectBooking timeEntryThree = projectTimeEntryFor(2, 8, 0, 11, 0);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo, timeEntryThree));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(1);
    }

    @Test
    void whenOneJourneyEntryActiveAnd10HoursRestTime_thenWarning() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(1, 16, 0, 22, 0);
        JourneyBooking timeEntryTwo = journeyTimeEntryFor(1, 22, 0, 2, 1, 0, Vehicle.CAR_ACTIVE);
        ProjectBooking timeEntryThree = projectTimeEntryFor(2, 8, 0, 11, 0);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo, timeEntryThree));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().hours()).isEqualTo(1);
    }

    @Test
    void whenJourneyEntryActiveAnd10HoursRestTime_thenWarning() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(1, 16, 0, 22, 0);
        JourneyBooking timeEntryTwo = journeyTimeEntryFor(2, 8, 0, 2, 9, 0, Vehicle.CAR_ACTIVE);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo));

        assertThat(warnings).isEmpty();
    }

    @Test
    void when11HoursRestTime_thenNoWarning() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(1, 16, 0, 22, 0);
        ProjectBooking timeEntryTwo = projectTimeEntryFor(2, 9, 0, 11, 0);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo));

        assertThat(warnings).isEmpty();
    }

    @Test
    void whenOneJourneyEntryAnd11HoursRestTime_thenNoWarning() {
        ProjectBooking timeEntryOne = projectTimeEntryFor(1, 16, 0, 22, 0);
        JourneyBooking timeEntryTwo = journeyTimeEntryFor(1, 22, 0, 2, 1, 0, Vehicle.CAR_INACTIVE);
        ProjectBooking timeEntryThree = projectTimeEntryFor(2, 9, 0, 11, 0);

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(timeEntryOne, timeEntryTwo, timeEntryThree));

        assertThat(warnings).isEmpty();
    }
}
