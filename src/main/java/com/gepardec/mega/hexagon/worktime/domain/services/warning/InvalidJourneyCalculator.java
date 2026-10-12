package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;
import com.gepardec.mega.hexagon.worktime.domain.services.Sequences;

import java.util.ArrayList;
import java.util.List;

public class InvalidJourneyCalculator implements WorkTimeWarningCalculator {
    @Override
    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings) {
        List<JourneyBooking> journeys = bookings.journeys();
        List<WorkTimeWarning> warnings = new ArrayList<>();
        JourneyDirectionScanner scanner = new JourneyDirectionScanner();
        for (var entry : Sequences.withSuccessor(journeys)) {
            JourneyBooking journey = entry.current();
            JourneyDirection subsequentJourneyDirection = entry.next().map(JourneyBooking::direction).orElse(null);
            WorkTimeWarningType warning = scanner.advance(journey.direction(), subsequentJourneyDirection);
            if (warning != null) {
                warnings.add(new WorkTimeWarning(journey.date(), warning, null));
            }
        }
        return warnings;
    }
}
