package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.LOCATION_RELEVANT_SET;

public class LocationRelevantSetJourneyCalculator implements WorkTimeWarningCalculator {
    @Override
    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings) {
        return bookings.values().stream()
                .filter(WorkTimeBooking::workLocationProjectRelevant)
                .map(WorkTimeBooking::date)
                .distinct()
                .map(date -> new WorkTimeWarning(date, LOCATION_RELEVANT_SET, null))
                .toList();
    }
}
