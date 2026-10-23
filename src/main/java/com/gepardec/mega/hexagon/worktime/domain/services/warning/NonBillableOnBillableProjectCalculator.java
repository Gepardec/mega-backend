package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.NON_BILLABLE_ON_BILLABLE_PROJECT;

public class NonBillableOnBillableProjectCalculator {
    private static final LocalDate RULE_START = LocalDate.of(2026, 10, 1);

    public List<WorkTimeWarning> calculate(WorkTimeBookings bookings, Set<Integer> billableProjectZepIds) {
        return bookings.values().stream()
                .filter(booking -> !booking.date().isBefore(RULE_START))
                .filter(booking -> !booking.billable())
                .filter(booking -> booking.projectZepId() != null
                        && billableProjectZepIds.contains(booking.projectZepId()))
                .map(booking -> new WorkTimeWarning(booking.date(), NON_BILLABLE_ON_BILLABLE_PROJECT, null))
                .distinct()
                .toList();
    }
}
