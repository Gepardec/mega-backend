package com.gepardec.mega.hexagon.worktime.domain.model;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;

class WorkTimeEvaluationTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 10);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);

    @Test
    void shouldRejectNullBookings() {
        assertThatNullPointerException()
                .isThrownBy(() -> new WorkTimeEvaluation(null, MONTH, TODAY, Set.of(), Set.of(), Set.of(), Set.of()))
                .withMessage("bookings must not be null");
    }

    @Test
    void shouldRejectNullPayrollMonth() {
        assertThatNullPointerException()
                .isThrownBy(() -> new WorkTimeEvaluation(WorkTimeBookings.empty(), null, TODAY, Set.of(), Set.of(), Set.of(), Set.of()))
                .withMessage("payrollMonth must not be null");
    }

    @Test
    void shouldRejectNullEvaluationDate() {
        assertThatNullPointerException()
                .isThrownBy(() -> new WorkTimeEvaluation(WorkTimeBookings.empty(), MONTH, null, Set.of(), Set.of(), Set.of(), Set.of()))
                .withMessage("evaluationDate must not be null");
    }

    @Test
    void shouldRejectNullSets() {
        var bookings = WorkTimeBookings.empty();
        assertThatNullPointerException()
                .isThrownBy(() -> new WorkTimeEvaluation(bookings, MONTH, TODAY, null, Set.of(), Set.of(), Set.of()))
                .withMessage("expectedWorkingDays must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new WorkTimeEvaluation(bookings, MONTH, TODAY, Set.of(), null, Set.of(), Set.of()))
                .withMessage("excusedDates must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new WorkTimeEvaluation(bookings, MONTH, TODAY, Set.of(), Set.of(), null, Set.of()))
                .withMessage("billableProjectZepIds must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new WorkTimeEvaluation(bookings, MONTH, TODAY, Set.of(), Set.of(), Set.of(), null))
                .withMessage("mealEntryDates must not be null");
    }

    @Test
    void shouldCopySetsIntoUnmodifiableSets() {
        LocalDate date = LocalDate.of(2026, 10, 5);
        Set<LocalDate> source = new HashSet<>(Set.of(date));

        WorkTimeEvaluation evaluation = Instancio.of(WorkTimeEvaluation.class)
                .set(field(WorkTimeEvaluation::expectedWorkingDays), source)
                .set(field(WorkTimeEvaluation::excusedDates), source)
                .set(field(WorkTimeEvaluation::mealEntryDates), source)
                .set(field(WorkTimeEvaluation::billableProjectZepIds), new HashSet<>(Set.of(1)))
                .create();
        source.clear();

        assertThat(evaluation.expectedWorkingDays()).containsExactly(date);
        assertThat(evaluation.excusedDates()).containsExactly(date);
        assertThat(evaluation.mealEntryDates()).containsExactly(date);
        assertThatThrownBy(() -> evaluation.expectedWorkingDays().add(date)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> evaluation.excusedDates().add(date)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> evaluation.mealEntryDates().add(date)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> evaluation.billableProjectZepIds().add(2)).isInstanceOf(UnsupportedOperationException.class);
    }
}
