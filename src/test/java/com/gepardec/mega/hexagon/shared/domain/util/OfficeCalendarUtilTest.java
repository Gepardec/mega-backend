package com.gepardec.mega.hexagon.shared.domain.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class OfficeCalendarUtilTest {

    @Test
    void isLastWorkingDayOfMonth_shouldReturnTrue_whenLastWorkingDayIsLastCalendarDay() {
        assertThat(OfficeCalendarUtil.isLastWorkingDayOfMonth(LocalDate.of(2024, 10, 31))).isTrue();
    }

    @Test
    void isLastWorkingDayOfMonth_shouldReturnTrue_whenLastWorkingDayIsBeforeWeekend() {
        assertThat(OfficeCalendarUtil.isLastWorkingDayOfMonth(LocalDate.of(2024, 8, 30))).isTrue();
    }

    @Test
    void isLastWorkingDayOfMonth_shouldReturnTrue_whenLastWorkingDayIsBeforePublicHoliday() {
        assertThat(OfficeCalendarUtil.isLastWorkingDayOfMonth(LocalDate.of(2021, 12, 30))).isTrue();
    }

    @Test
    void isLastWorkingDayOfMonth_shouldReturnFalse_whenDateIsMidMonth() {
        assertThat(OfficeCalendarUtil.isLastWorkingDayOfMonth(LocalDate.of(2024, 10, 15))).isFalse();
    }
}
