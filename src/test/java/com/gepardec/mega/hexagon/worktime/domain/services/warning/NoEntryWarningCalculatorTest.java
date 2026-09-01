package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NoEntryWarningCalculatorTest {
    private final NoEntryWarningCalculator calculator = new NoEntryWarningCalculator();

    @Test
    void calculate_returnsOnlyPastExpectedUnbookedUnexcusedDays() {
        LocalDate pastMissing = LocalDate.of(2026, 5, 4);
        LocalDate booked = LocalDate.of(2026, 5, 5);
        LocalDate excused = LocalDate.of(2026, 5, 6);
        LocalDate today = LocalDate.of(2026, 5, 7);
        LocalDate future = LocalDate.of(2026, 5, 8);

        assertThat(calculator.calculate(Set.of(pastMissing, booked, excused, today, future),
                Set.of(booked), Set.of(excused), today))
                .containsExactly(new com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning(
                        pastMissing, WorkTimeWarningType.NO_TIME_ENTRY, null));
    }
}
