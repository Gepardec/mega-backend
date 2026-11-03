package com.gepardec.mega.hexagon.worktime.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class WorkTimeWarningsTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 7, 13);

    @Test
    void shouldKeepFirstWarningPerTypeAndDateInOriginalOrder() {
        WorkTimeWarning first = new WorkTimeWarning(MONDAY, WorkTimeWarningType.TIME_OVERLAP, 1.0);
        WorkTimeWarning otherType = new WorkTimeWarning(MONDAY, WorkTimeWarningType.WEEKEND, null);
        WorkTimeWarning duplicate = new WorkTimeWarning(MONDAY, WorkTimeWarningType.TIME_OVERLAP, 2.0);
        WorkTimeWarning otherDate = new WorkTimeWarning(MONDAY.plusDays(1), WorkTimeWarningType.TIME_OVERLAP, null);
        WorkTimeWarning undated = new WorkTimeWarning(null, WorkTimeWarningType.TIME_OVERLAP, null);
        WorkTimeWarning undatedDuplicate = new WorkTimeWarning(null, WorkTimeWarningType.TIME_OVERLAP, 3.0);

        WorkTimeWarnings warnings = new WorkTimeWarnings(
                List.of(first, otherType, duplicate, otherDate, undated, undatedDuplicate));

        assertThat(warnings.values()).containsExactly(first, otherType, otherDate, undated);
    }

    @Test
    void shouldDefensivelyCopyAndCompareByValue() {
        WorkTimeWarning warning = new WorkTimeWarning(MONDAY, WorkTimeWarningType.TIME_OVERLAP, null);
        List<WorkTimeWarning> source = new ArrayList<>(List.of(warning));

        WorkTimeWarnings warnings = new WorkTimeWarnings(source);
        source.clear();

        assertThat(warnings.values()).isUnmodifiable().containsExactly(warning);
        assertThat(warnings).isEqualTo(new WorkTimeWarnings(List.of(warning)));
    }

    @Test
    void shouldRejectNullCollectionAndElements() {
        assertThatNullPointerException().isThrownBy(() -> new WorkTimeWarnings(null));
        assertThatNullPointerException().isThrownBy(() -> new WorkTimeWarnings(Arrays.asList((WorkTimeWarning) null)));
    }

    @Test
    void noEntries_containsOnlyMonthLevelEmptyEntryListWarning() {
        assertThat(WorkTimeWarnings.noEntries().values())
                .containsExactly(new WorkTimeWarning(null, WorkTimeWarningType.EMPTY_ENTRY_LIST, null));
    }
}
