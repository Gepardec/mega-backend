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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static org.assertj.core.api.Assertions.assertThat;

class DoctorAppointmentCalculatorTest {

    private static final LocalDate DATE = LocalDate.of(2020, 1, 7);
    private static final String DOCTOR_APPOINTMENT_PROCESS = "233";
    private static final String OTHER_PROCESS = "42";

    private final DoctorAppointmentCalculator calculator = new DoctorAppointmentCalculator();

    @ParameterizedTest
    @CsvSource({
            "08:30, 12:00",
            "12:30, 17:00",
            "09:00, 11:00",
            "13:00, 16:00",
            "10:15, 10:45"
    })
    void calculate_whenAppointmentWithinAnAllowedWindow_thenNoWarning(LocalTime from, LocalTime to) {
        List<WorkTimeWarning> warnings = calculator.calculate(
                bookings(doctorAppointment(from, to)));

        assertThat(warnings).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "08:00, 09:00",
            "11:00, 12:15",
            "12:15, 13:00",
            "16:00, 17:30",
            "11:00, 13:00"
    })
    void calculate_whenAppointmentOutsideTheAllowedWindows_thenWarning(LocalTime from, LocalTime to) {
        List<WorkTimeWarning> warnings = calculator.calculate(
                bookings(doctorAppointment(from, to)));

        assertThat(warnings).singleElement().satisfies(warning -> {
            assertThat(warning.type()).isEqualTo(WorkTimeWarningType.WRONG_DOCTOR_APPOINTMENT);
            assertThat(warning.date()).isEqualTo(DATE);
            assertThat(warning.hours()).isNull();
        });
    }

    @Test
    void calculate_whenBookingOutsideTheAllowedWindowsIsNotADoctorAppointment_thenNoWarning() {
        List<WorkTimeWarning> warnings = calculator.calculate(
                bookings(projectBooking(LocalTime.of(6, 0), LocalTime.of(7, 0), OTHER_PROCESS)));

        assertThat(warnings).isEmpty();
    }

    @Test
    void calculate_whenJourneyBookingOutsideTheAllowedWindows_thenNoWarning() {
        List<WorkTimeWarning> warnings = calculator.calculate(bookings(journeyBooking()));

        assertThat(warnings).isEmpty();
    }

    @Test
    void calculate_whenSeveralAppointmentsOnDifferentDates_thenOneWarningPerOffendingAppointment() {
        ProjectBooking tooEarly = doctorAppointment(LocalTime.of(8, 0), LocalTime.of(9, 0));
        ProjectBooking allowed = doctorAppointment(LocalTime.of(13, 0), LocalTime.of(14, 0));
        ProjectBooking tooLateOnNextDay = doctorAppointmentOn(
                DATE.plusDays(1), LocalTime.of(16, 0), LocalTime.of(17, 30));

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(tooEarly, allowed, tooLateOnNextDay));

        assertThat(warnings)
                .extracting(WorkTimeWarning::date)
                .containsExactly(DATE, DATE.plusDays(1));
    }

    @Test
    void calculate_whenNoBookings_thenNoWarning() {
        List<WorkTimeWarning> warnings = calculator.calculate(bookings());

        assertThat(warnings).isEmpty();
    }

    private ProjectBooking doctorAppointment(LocalTime from, LocalTime to) {
        return doctorAppointmentOn(DATE, from, to);
    }

    private ProjectBooking doctorAppointmentOn(LocalDate date, LocalTime from, LocalTime to) {
        return projectBooking(date, from, to, DOCTOR_APPOINTMENT_PROCESS);
    }

    private ProjectBooking projectBooking(LocalTime from, LocalTime to, String process) {
        return projectBooking(DATE, from, to, process);
    }

    private ProjectBooking projectBooking(LocalDate date, LocalTime from, LocalTime to, String process) {
        return WarningTestBookingBuilder.projectBookingBuilder()
                .fromTime(LocalDateTime.of(date, from))
                .toTime(LocalDateTime.of(date, to))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
                .workLocationIsProjectRelevant(false)
                .process(process)
                .build();
    }

    private JourneyBooking journeyBooking() {
        return WarningTestBookingBuilder.journeyBookingBuilder()
                .fromTime(LocalDateTime.of(DATE, LocalTime.of(6, 0)))
                .toTime(LocalDateTime.of(DATE, LocalTime.of(7, 0)))
                .task(Task.REISEN)
                .workingLocation(WorkingLocation.MAIN)
                .workLocationIsProjectRelevant(false)
                .journeyDirection(JourneyDirection.TO)
                .vehicle(Vehicle.CAR_ACTIVE)
                .build();
    }
}
