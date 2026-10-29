package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEvaluation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class InternalNoteConventionCalculator implements WorkTimeWarningCalculator {
    private static final Pattern INTERNAL_MARKER = Pattern.compile("^\\s*intern\\s*[-–:]", Pattern.CASE_INSENSITIVE);

    @Override
    public WarningRule rule() {
        return WarningRule.INTERNAL_NOTE;
    }

    @Override
    public List<WorkTimeWarning> calculate(WorkTimeEvaluation evaluation) {
        boolean containsBillableWork = containsBillableWork(evaluation);
        Set<WorkTimeWarning> warnings = new LinkedHashSet<>();
        for (WorkTimeBooking booking : evaluation.bookings()) {
            boolean markedInternal = isMarkedInternal(booking.note());
            if (booking.billable() && markedInternal) {
                warnings.add(new WorkTimeWarning(booking.date(), WorkTimeWarningType.INTERNAL_NOTE_BILLABLE, null));
            } else if (containsBillableWork && !booking.billable() && !markedInternal
                    && !isOnBillableProject(booking, evaluation)) {
                warnings.add(new WorkTimeWarning(booking.date(), WorkTimeWarningType.INTERNAL_NOTE_MISSING, null));
            }
        }
        return List.copyOf(warnings);
    }

    private static boolean containsBillableWork(WorkTimeEvaluation evaluation) {
        for (WorkTimeBooking booking : evaluation.bookings()) {
            if (booking.billable() || isOnBillableProject(booking, evaluation)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOnBillableProject(WorkTimeBooking booking, WorkTimeEvaluation evaluation) {
        return booking.projectZepId() != null && evaluation.billableProjectZepIds().contains(booking.projectZepId());
    }

    private static boolean isMarkedInternal(String note) {
        return note != null && INTERNAL_MARKER.matcher(note).find();
    }
}
