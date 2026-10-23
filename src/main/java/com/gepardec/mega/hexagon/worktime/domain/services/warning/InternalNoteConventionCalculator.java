package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class InternalNoteConventionCalculator implements BookingsWarningCalculator {
    private static final LocalDate CONVENTION_START = LocalDate.of(2026, 10, 1);
    private static final Pattern INTERNAL_MARKER = Pattern.compile("^\\s*intern\\s*[-–]", Pattern.CASE_INSENSITIVE);

    @Override
    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings) {
        Set<WorkTimeWarning> warnings = new LinkedHashSet<>();
        for (WorkTimeBooking booking : bookings) {
            if (booking.date().isBefore(CONVENTION_START)) {
                continue;
            }
            boolean markedInternal = isMarkedInternal(booking.note());
            if (booking.billable() && markedInternal) {
                warnings.add(new WorkTimeWarning(booking.date(), WorkTimeWarningType.INTERNAL_NOTE_BILLABLE, null));
            } else if (!booking.billable() && !markedInternal) {
                warnings.add(new WorkTimeWarning(booking.date(), WorkTimeWarningType.INTERNAL_NOTE_MISSING, null));
            }
        }
        return List.copyOf(warnings);
    }

    private static boolean isMarkedInternal(String note) {
        return note != null && INTERNAL_MARKER.matcher(note).find();
    }
}
