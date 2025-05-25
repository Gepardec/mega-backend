package com.gepardec.mega.domain.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public record EmploymentPeriods(List<EmploymentPeriod> employmentPeriods) {

    public static EmploymentPeriods empty() {
        return new EmploymentPeriods(List.of());
    }

    public EmploymentPeriods(EmploymentPeriod employmentPeriod) {
        this(List.of(employmentPeriod));
    }

    /**
     * Returns the latest employment period based on the start date.
     * If no employment periods exist, returns an empty Optional.
     *
     * @return Optional containing the latest employment period or empty if none exist
     */
    public Optional<EmploymentPeriod> latest() {
        return employmentPeriods.stream().max(Comparator.comparing(EmploymentPeriod::start));
    }

    /**
     * Returns the active employment period for the given reference date.
     * If no active period exists, returns an empty Optional.
     * An employment period is considered active if its start date is in the past
     * and its end date is either null or in the future.
     *
     * @param referenceDate the date to check for active employment
     * @return Optional containing the active employment period or empty if none is active
     */
    public Optional<EmploymentPeriod> active(LocalDate referenceDate) {
        return employmentPeriods.stream()
                .filter(isStartInPast(referenceDate).and(isOpenOrEndsInFuture(referenceDate)))
                .max(Comparator.comparing(EmploymentPeriod::start));
    }

    /**
     * Returns the active employment period for the given payroll month.
     * If no active period exists, returns an empty Optional.
     * An employment period is considered active if its start date is in the past
     * and its end date is either null or in the future.
     *
     * @param payrollMonth the month to check for active employment
     * @return Optional containing the active employment period or empty if none is active
     */
    public Optional<EmploymentPeriod> active(YearMonth payrollMonth) {
        return active(payrollMonth.atDay(1)).or(() -> active(payrollMonth.atEndOfMonth()));
    }

    private Predicate<EmploymentPeriod> isStartInPast(LocalDate referenceDate) {
        return zepEmploymentPeriod -> {
            if (zepEmploymentPeriod.start() == null) {
                return false;
            }

            return !zepEmploymentPeriod.start().isAfter(referenceDate);
        };
    }

    private Predicate<EmploymentPeriod> isOpenOrEndsInFuture(LocalDate referenceDate) {
        return zepEmploymentPeriod -> {
            if (zepEmploymentPeriod.end() == null) {
                return true;
            }

            return !zepEmploymentPeriod.end().isBefore(referenceDate);
        };
    }
}
