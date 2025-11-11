package com.gepardec.mega.domain.model;

import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public record RegularWorkingTimes(List<RegularWorkingTime> regularWorkingTimes) {

    public static RegularWorkingTimes empty() {
        return new RegularWorkingTimes(List.of());
    }

    public RegularWorkingTimes(RegularWorkingTime regularWorkingTime) {
        this(List.of(regularWorkingTime));
    }

    /**
     * Returns the latest regular working time based on the start date.
     * If there are no regular working times, it returns an empty Optional.
     *
     * @return Optional containing the latest regular working time or empty if none exist
     */
    public Optional<RegularWorkingTime> latest() {
        return regularWorkingTimes.stream().max(Comparator.comparing(RegularWorkingTime::start));
    }

    /**
     * Returns the active regular working time for the given reference date.
     * If there is only one regular working time entry with a null start date,
     * it is considered active from the start of the employment period.
     *
     * @param referenceDate the date to check for active regular working time
     * @return Optional containing the active regular working time or empty if none are active
     */
    public Optional<RegularWorkingTime> active(LocalDate referenceDate) {
        if (CollectionUtils.isNotEmpty(regularWorkingTimes)
                && regularWorkingTimes.size() == 1
                && regularWorkingTimes.getFirst().start() == null) {
            return Optional.of(regularWorkingTimes.getFirst());
        }

        return regularWorkingTimes.stream()
                .filter(isStartInPast(referenceDate))
                .max(
                        Comparator.comparing(
                                RegularWorkingTime::start,
                                Comparator.nullsFirst(Comparator.naturalOrder())
                        )
                );
    }

    /**
     * Returns the active regular working time for the given payroll month.
     * This method first checks for an active working time at the start of the month,
     * and if none is found, checks at the end of the month. This ensures that working
     * times starting mid-month are correctly identified.
     * 
     * @param payrollMonth the month to check for active regular working time
     * @return Optional containing the active regular working time or empty if none are active
     */
    public Optional<RegularWorkingTime> active(YearMonth payrollMonth) {
        return active(payrollMonth.atDay(1)).or(() -> active(payrollMonth.atEndOfMonth()));
    }

    private Predicate<RegularWorkingTime> isStartInPast(LocalDate referenceDate) {
        return zepRegularWorkingHours -> {
            if (zepRegularWorkingHours.start() == null) {
                return false;
            }

            return !zepRegularWorkingHours.start().isAfter(referenceDate);
        };
    }
}
