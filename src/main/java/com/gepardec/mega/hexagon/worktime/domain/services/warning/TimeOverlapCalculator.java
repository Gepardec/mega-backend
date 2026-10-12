package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.TIME_OVERLAP;

public class TimeOverlapCalculator implements WorkTimeWarningCalculator {

    private static final int MINIMUM_BOOKINGS = 2;

    @Override
    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings) {
        List<WorkTimeWarning> warnings = new ArrayList<>();
        bookings.byDate().forEach((date, dayBookings) -> {
            if (hasOverlap(dayBookings.values())) {
                warnings.add(new WorkTimeWarning(date, TIME_OVERLAP, null));
            }
        });
        return List.copyOf(warnings);
    }

    private boolean hasOverlap(List<WorkTimeBooking> sortedByFrom) {
        if (sortedByFrom.size() < MINIMUM_BOOKINGS) {
            return false;
        }
        LocalDateTime maxEnd = sortedByFrom.getFirst().to();
        for (int i = 1; i < sortedByFrom.size(); i++) {
            WorkTimeBooking current = sortedByFrom.get(i);
            if (current.from().isBefore(maxEnd)) {
                return true;
            }
            if (current.to().isAfter(maxEnd)) {
                maxEnd = current.to();
            }
        }
        return false;
    }
}
