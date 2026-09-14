package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.util.ArrayList;
import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.OUTSIDE_CORE_WORKING_TIME;

public class CoreWorkingHoursCalculator implements WorkTimeWarningCalculator {
    static final int EARLIEST_HOUR = 6;
    static final int LATEST_HOUR = 22;

    @Override
    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings) {
        List<WorkTimeWarning> warnings = new ArrayList<>();
        bookings.contributingToWorkingTime().byDate().forEach((date, dayBookings) -> {
            WorkTimeBooking first = dayBookings.values().getFirst();
            WorkTimeBooking last = dayBookings.values().getLast();
            if ((startsTooEarly(first) || finishesTooLate(last)) && !isZeroDurationJourneyBoundary(first, last)) {
                warnings.add(new WorkTimeWarning(date, OUTSIDE_CORE_WORKING_TIME, null));
            }
        });
        return warnings;
    }

    private boolean isZeroDurationJourneyBoundary(WorkTimeBooking first, WorkTimeBooking last) {
        return first instanceof JourneyBooking && first.durationInHours() == 0
                || last instanceof ProjectBooking && last.durationInHours() == 0;
    }

    private boolean startsTooEarly(WorkTimeBooking booking) {
        return booking.from().getHour() < EARLIEST_HOUR;
    }

    private boolean finishesTooLate(WorkTimeBooking booking) {
        return booking.to().getHour() > LATEST_HOUR;
    }
}
