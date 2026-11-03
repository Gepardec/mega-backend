package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.INTERNAL_PREFIX_MISSING;
import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.INTERNAL_PREFIX_ON_BILLABLE_PROJECT;

public class InternalNotePrefixCalculator {
    private static final Pattern INTERNAL_PREFIX = Pattern.compile(
            "^\\s*intern\\s*[-–—]", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings, Map<Integer, Boolean> billableByProjectZepId) {
        Objects.requireNonNull(bookings, "bookings must not be null");
        Objects.requireNonNull(billableByProjectZepId, "billableByProjectZepId must not be null");
        return bookings.values().stream()
                .map(booking -> violation(booking, billableByProjectZepId)
                        .map(type -> new WorkTimeWarning(booking.date(), type, null)))
                .flatMap(Optional::stream)
                .distinct()
                .toList();
    }

    private static Optional<WorkTimeWarningType> violation(WorkTimeBooking booking, Map<Integer, Boolean> billableByProjectZepId) {
        Boolean billable = booking.projectZepId() == null ? null : billableByProjectZepId.get(booking.projectZepId());
        if (billable == null) {
            return Optional.empty();
        }
        boolean prefixed = hasInternalPrefix(booking.note());
        if (billable && prefixed) {
            return Optional.of(INTERNAL_PREFIX_ON_BILLABLE_PROJECT);
        }
        if (!billable && !prefixed) {
            return Optional.of(INTERNAL_PREFIX_MISSING);
        }
        return Optional.empty();
    }

    private static boolean hasInternalPrefix(String note) {
        return note != null && INTERNAL_PREFIX.matcher(note).find();
    }
}
