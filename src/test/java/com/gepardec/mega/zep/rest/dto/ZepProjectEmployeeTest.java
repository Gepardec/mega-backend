package com.gepardec.mega.zep.rest.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ZepProjectEmployeeTest {

    @Test
    void isActive_whenBothFromAndToAreNull_thenReturnsTrue() {
        // Given
        YearMonth payrollMonth = YearMonth.of(2026, 1);
        ZepProjectEmployee employee = ZepProjectEmployee.builder()
                .username("testuser")
                .from(null)
                .to(null)
                .build();

        // When
        boolean result = employee.isActive(payrollMonth);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isActive_whenActiveForEntireMonth_thenReturnsTrue() {
        // Given
        YearMonth payrollMonth = YearMonth.of(2026, 1);
        LocalDateTime from = LocalDateTime.of(2025, 12, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 2, 28, 23, 59);

        ZepProjectEmployee employee = ZepProjectEmployee.builder()
                .username("testuser")
                .from(from)
                .to(to)
                .build();

        // When
        boolean result = employee.isActive(payrollMonth);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isActive_whenStartsOnFirstDayOfMonth_thenReturnsTrue() {
        // Given
        YearMonth payrollMonth = YearMonth.of(2026, 1);
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 12, 31, 23, 59);

        ZepProjectEmployee employee = ZepProjectEmployee.builder()
                .username("testuser")
                .from(from)
                .to(to)
                .build();

        // When
        boolean result = employee.isActive(payrollMonth);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isActive_whenEndsOnLastDayOfMonth_thenReturnsTrue() {
        // Given
        YearMonth payrollMonth = YearMonth.of(2026, 1);
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 1, 31, 23, 59);

        ZepProjectEmployee employee = ZepProjectEmployee.builder()
                .username("testuser")
                .from(from)
                .to(to)
                .build();

        // When
        boolean result = employee.isActive(payrollMonth);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isActive_whenToIsNull_thenReturnsTrue() {
        // Given
        YearMonth payrollMonth = YearMonth.of(2026, 1);
        LocalDateTime from = LocalDateTime.of(2025, 12, 1, 0, 0);

        ZepProjectEmployee employee = ZepProjectEmployee.builder()
                .username("testuser")
                .from(from)
                .to(null)
                .build();

        // When
        boolean result = employee.isActive(payrollMonth);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isActive_whenStartsAfterMonthEnds_thenReturnsFalse() {
        // Given
        YearMonth payrollMonth = YearMonth.of(2026, 1);
        LocalDateTime from = LocalDateTime.of(2026, 2, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 12, 31, 23, 59);

        ZepProjectEmployee employee = ZepProjectEmployee.builder()
                .username("testuser")
                .from(from)
                .to(to)
                .build();

        // When
        boolean result = employee.isActive(payrollMonth);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isActive_whenEndedBeforeMonthStarts_thenReturnsFalse() {
        // Given
        YearMonth payrollMonth = YearMonth.of(2026, 1);
        LocalDateTime from = LocalDateTime.of(2025, 10, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2025, 12, 31, 23, 59);

        ZepProjectEmployee employee = ZepProjectEmployee.builder()
                .username("testuser")
                .from(from)
                .to(to)
                .build();

        // When
        boolean result = employee.isActive(payrollMonth);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isActive_whenActiveOnlyOnFirstDay_thenReturnsTrue() {
        // Given
        YearMonth payrollMonth = YearMonth.of(2026, 1);
        LocalDateTime from = LocalDateTime.of(2025, 12, 15, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 1, 1, 23, 59);

        ZepProjectEmployee employee = ZepProjectEmployee.builder()
                .username("testuser")
                .from(from)
                .to(to)
                .build();

        // When
        boolean result = employee.isActive(payrollMonth);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isActive_whenActiveOnlyOnLastDay_thenReturnsTrue() {
        // Given
        YearMonth payrollMonth = YearMonth.of(2026, 1);
        LocalDateTime from = LocalDateTime.of(2026, 1, 31, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 2, 28, 23, 59);

        ZepProjectEmployee employee = ZepProjectEmployee.builder()
                .username("testuser")
                .from(from)
                .to(to)
                .build();

        // When
        boolean result = employee.isActive(payrollMonth);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isActive_whenPeriodEntirelyWithinMonth_thenReturnsTrue() {
        // Given
        YearMonth payrollMonth = YearMonth.of(2026, 1);
        LocalDateTime from = LocalDateTime.of(2026, 1, 10, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 1, 20, 23, 59);

        ZepProjectEmployee employee = ZepProjectEmployee.builder()
                .username("testuser")
                .from(from)
                .to(to)
                .build();

        // When
        boolean result = employee.isActive(payrollMonth);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isActive_whenFebruaryInLeapYear_thenHandlesCorrectly() {
        // Given - 2024 is a leap year
        YearMonth payrollMonth = YearMonth.of(2024, 2);
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 2, 29, 23, 59);

        ZepProjectEmployee employee = ZepProjectEmployee.builder()
                .username("testuser")
                .from(from)
                .to(to)
                .build();

        // When
        boolean result = employee.isActive(payrollMonth);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isActive_whenFebruaryInNonLeapYear_thenHandlesCorrectly() {
        // Given - 2026 is not a leap year
        YearMonth payrollMonth = YearMonth.of(2026, 2);
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 2, 28, 23, 59);

        ZepProjectEmployee employee = ZepProjectEmployee.builder()
                .username("testuser")
                .from(from)
                .to(to)
                .build();

        // When
        boolean result = employee.isActive(payrollMonth);

        // Then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideBoundaryTestCases")
    void isActive_boundaryConditions(LocalDateTime from, LocalDateTime to, YearMonth payrollMonth, boolean expected) {
        // Given
        ZepProjectEmployee employee = ZepProjectEmployee.builder()
                .username("testuser")
                .from(from)
                .to(to)
                .build();

        // When
        boolean result = employee.isActive(payrollMonth);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> provideBoundaryTestCases() {
        YearMonth january2026 = YearMonth.of(2026, 1);

        return Stream.of(
                // Employee ends exactly one day before month starts
                Arguments.of(
                        LocalDateTime.of(2025, 10, 1, 0, 0),
                        LocalDateTime.of(2025, 12, 31, 0, 0),
                        january2026,
                        false
                ),
                // Employee starts exactly one day after month ends
                Arguments.of(
                        LocalDateTime.of(2026, 2, 1, 0, 0),
                        LocalDateTime.of(2026, 3, 31, 0, 0),
                        january2026,
                        false
                ),
                // Employee starts on the last day of previous month
                Arguments.of(
                        LocalDateTime.of(2025, 12, 31, 23, 59),
                        LocalDateTime.of(2026, 2, 28, 0, 0),
                        january2026,
                        true
                ),
                // Employee ends on the first day of next month
                Arguments.of(
                        LocalDateTime.of(2025, 12, 1, 0, 0),
                        LocalDateTime.of(2026, 2, 1, 0, 0),
                        january2026,
                        true
                ),
                // From is null, to is in the future
                Arguments.of(
                        null,
                        LocalDateTime.of(2026, 12, 31, 0, 0),
                        january2026,
                        true
                ),
                // From is null, to is in the past
                Arguments.of(
                        null,
                        LocalDateTime.of(2025, 12, 31, 0, 0),
                        january2026,
                        false
                ),
                // From is in the future, to is null
                Arguments.of(
                        LocalDateTime.of(2026, 2, 1, 0, 0),
                        null,
                        january2026,
                        false
                ),
                // From is in the past, to is null
                Arguments.of(
                        LocalDateTime.of(2025, 12, 1, 0, 0),
                        null,
                        january2026,
                        true
                )
        );
    }
}
