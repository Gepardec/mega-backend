package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.WRONG_DOCTOR_APPOINTMENT;

public class DoctorAppointmentCalculator implements WorkTimeWarningCalculator {
    private static final List<PermittedWindow> PERMITTED_WINDOWS = List.of(
            new PermittedWindow(LocalTime.of(8, 30), LocalTime.NOON),
            new PermittedWindow(LocalTime.of(12, 30), LocalTime.of(17, 0)));
    private static final String DOCTOR_APPOINTMENT_PROCESS = "233";

    @Override
    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings) {
        return bookings.projects().stream()
                .filter(booking -> DOCTOR_APPOINTMENT_PROCESS.equals(booking.process()))
                .filter(booking -> PERMITTED_WINDOWS.stream().noneMatch(window -> window.contains(booking)))
                .map(booking -> new WorkTimeWarning(booking.date(), WRONG_DOCTOR_APPOINTMENT, null))
                .toList();
    }

    private record PermittedWindow(LocalTime start, LocalTime end) {
        private boolean contains(ProjectBooking booking) {
            LocalDateTime windowStart = booking.date().atTime(start);
            LocalDateTime windowEnd = booking.date().atTime(end);
            return !booking.from().isBefore(windowStart) && !booking.to().isAfter(windowEnd);
        }
    }
}
