package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.time.LocalTime;
import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.WRONG_DOCTOR_APPOINTMENT;

public class DoctorAppointmentCalculator implements WorkTimeWarningCalculator {
    private static final LocalTime START_MORNING = LocalTime.of(8, 30);
    private static final LocalTime END_MORNING = LocalTime.NOON;
    private static final LocalTime START_AFTERNOON = LocalTime.of(12, 30);
    private static final LocalTime END_AFTERNOON = LocalTime.of(17, 0);
    private static final String DOCTOR_APPOINTMENT_PROCESS = "233";

    @Override
    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings) {
        return bookings.projects().stream()
                .filter(booking -> DOCTOR_APPOINTMENT_PROCESS.equals(booking.process()))
                .filter(this::isOutsideAllowedWindow)
                .map(booking -> new WorkTimeWarning(booking.date(), WRONG_DOCTOR_APPOINTMENT, null))
                .toList();
    }

    private boolean isOutsideAllowedWindow(ProjectBooking booking) {
        LocalTime from = booking.from().toLocalTime();
        LocalTime to = booking.to().toLocalTime();
        return from.isBefore(START_MORNING)
                || to.isAfter(END_MORNING) && to.isBefore(START_AFTERNOON)
                || from.isAfter(END_MORNING) && from.isBefore(START_AFTERNOON)
                || to.isAfter(END_AFTERNOON)
                || from.isBefore(END_MORNING) && to.isAfter(START_AFTERNOON);
    }
}
