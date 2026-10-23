package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEvaluation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.time.LocalDate;
import java.util.List;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.NON_BILLABLE_ON_BILLABLE_PROJECT;

public class NonBillableOnBillableProjectCalculator implements WorkTimeWarningCalculator {
    private static final LocalDate RULE_START = LocalDate.of(2026, 10, 1);

    @Override
    public List<WorkTimeWarning> calculate(WorkTimeEvaluation evaluation) {
        return evaluation.bookings().values().stream()
                .filter(booking -> !booking.date().isBefore(RULE_START))
                .filter(booking -> !booking.billable())
                .filter(booking -> booking.projectZepId() != null
                        && evaluation.billableProjectZepIds().contains(booking.projectZepId()))
                .map(booking -> new WorkTimeWarning(booking.date(), NON_BILLABLE_ON_BILLABLE_PROJECT, null))
                .distinct()
                .toList();
    }
}
