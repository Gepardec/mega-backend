package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;

import java.util.ArrayList;
import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.INVALID_WORKING_LOCATION;

public class InvalidWorkingLocationInJourneyCalculator implements WorkTimeWarningCalculator {
    @Override
    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings) {
        if (bookings.journeys().isEmpty()) {
            return List.of();
        }
        List<WorkTimeWarning> warnings = new ArrayList<>();
        WorkingLocation expected = WorkingLocation.MAIN;
        for (WorkTimeBooking booking : bookings) {
            switch (booking) {
                case JourneyBooking journey when journey.direction() == JourneyDirection.BACK ->
                        expected = WorkingLocation.MAIN;
                case JourneyBooking journey -> expected = journey.workingLocation();
                default -> {
                    if (booking.workingLocation() != expected) {
                        warnings.add(new WorkTimeWarning(booking.date(), INVALID_WORKING_LOCATION, null));
                    }
                }
            }
        }
        return warnings;
    }
}
