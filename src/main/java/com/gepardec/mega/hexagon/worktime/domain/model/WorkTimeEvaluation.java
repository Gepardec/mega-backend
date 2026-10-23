package com.gepardec.mega.hexagon.worktime.domain.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Set;

public record WorkTimeEvaluation(
        WorkTimeBookings bookings,
        YearMonth payrollMonth,
        LocalDate evaluationDate,
        Set<LocalDate> expectedWorkingDays,
        Set<LocalDate> excusedDates,
        Set<Integer> billableProjectZepIds,
        Set<LocalDate> mealEntryDates) {

    public WorkTimeEvaluation {
        Objects.requireNonNull(bookings, "bookings must not be null");
        Objects.requireNonNull(payrollMonth, "payrollMonth must not be null");
        Objects.requireNonNull(evaluationDate, "evaluationDate must not be null");
        Objects.requireNonNull(expectedWorkingDays, "expectedWorkingDays must not be null");
        Objects.requireNonNull(excusedDates, "excusedDates must not be null");
        Objects.requireNonNull(billableProjectZepIds, "billableProjectZepIds must not be null");
        Objects.requireNonNull(mealEntryDates, "mealEntryDates must not be null");
        expectedWorkingDays = Set.copyOf(expectedWorkingDays);
        excusedDates = Set.copyOf(excusedDates);
        billableProjectZepIds = Set.copyOf(billableProjectZepIds);
        mealEntryDates = Set.copyOf(mealEntryDates);
    }
}
