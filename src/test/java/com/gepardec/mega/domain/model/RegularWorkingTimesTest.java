package com.gepardec.mega.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RegularWorkingTimesTest {

    @Nested
    @DisplayName("Tests for latest() method")
    class Latest {

        @Test
        void latest_WhenGivenMultipleRegularWorkingHours_ThenShouldReturnLatestByStartDate() {
            // Arrange
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(LocalDate.of(2023, 1, 1), workingHours1),
                            new RegularWorkingTime(LocalDate.of(2023, 7, 1), workingHours2)
                    )
            );

            // Act & Assert
            assertThat(regularWorkingTimes.latest())
                    .isPresent()
                    .hasValueSatisfying(hours -> {
                        assertThat(hours.start()).isEqualTo(LocalDate.of(2023, 7, 1));
                        assertThat(hours.workingHours()).isEqualTo(workingHours2);
                    });
        }

        @Test
        void latest_WhenGivenEmptyList_ThenShouldReturnEmptyOptional() {
            // Arrange
            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(Collections.emptyList());

            // Act & Assert
            assertThat(regularWorkingTimes.latest()).isEmpty();
        }

        @Test
        void latest_WhenAllHoursHaveSameStartDate_ThenShouldReturnAny() {
            // Arrange
            LocalDate sameStartDate = LocalDate.of(2023, 1, 1);
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(sameStartDate, workingHours1),
                            new RegularWorkingTime(sameStartDate, workingHours2)
                    )
            );

            // Act & Assert
            assertThat(regularWorkingTimes.latest())
                    .isPresent()
                    .hasValueSatisfying(hours -> {
                        assertThat(hours.start()).isEqualTo(sameStartDate);
                    });
        }
    }

    @Nested
    @DisplayName("Tests for active() method")
    class Active {

        @Test
        void active_WhenSingleEntryWithNullStartDate_ThenShouldReturnThatEntry() {
            // Arrange
            LocalDate referenceDate = LocalDate.of(2023, 8, 15);
            Map<DayOfWeek, Duration> workingHours = createWorkingHoursMap(8);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(null, workingHours)
                    )
            );

            // Act & Assert
            assertThat(regularWorkingTimes.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(hours -> {
                        assertThat(hours.start()).isNull();
                        assertThat(hours.workingHours()).isEqualTo(workingHours);
                    });
        }

        @Test
        void active_WhenMultipleEntriesIncludingOneWithNullStartDate_ThenShouldReturnLatestByStartDate() {
            // Arrange
            LocalDate referenceDate = LocalDate.of(2023, 8, 15);
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(LocalDate.of(2023, 1, 1), workingHours1),
                            new RegularWorkingTime(null, workingHours2)
                    )
            );

            // Act & Assert
            assertThat(regularWorkingTimes.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(hours -> {
                        assertThat(hours.start()).isEqualTo(LocalDate.of(2023, 1, 1));
                        assertThat(hours.workingHours()).isEqualTo(workingHours1);
                    });
        }

        @Test
        void active_WhenReferenceIsAfterStartDate_ThenShouldReturnLatestApplicableEntry() {
            // Arrange
            LocalDate referenceDate = LocalDate.of(2023, 8, 15);
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(LocalDate.of(2023, 1, 1), workingHours1),
                            new RegularWorkingTime(LocalDate.of(2023, 7, 1), workingHours2)
                    )
            );

            // Act & Assert
            assertThat(regularWorkingTimes.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(hours -> {
                        assertThat(hours.start()).isEqualTo(LocalDate.of(2023, 7, 1));
                        assertThat(hours.workingHours()).isEqualTo(workingHours2);
                    });
        }

        @Test
        void active_WhenReferenceEqualsStartDate_ThenShouldReturnThatEntry() {
            // Arrange
            LocalDate referenceDate = LocalDate.of(2023, 7, 1);
            Map<DayOfWeek, Duration> workingHours = createWorkingHoursMap(8);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(referenceDate, workingHours)
                    )
            );

            // Act & Assert
            assertThat(regularWorkingTimes.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(hours -> {
                        assertThat(hours.start()).isEqualTo(referenceDate);
                        assertThat(hours.workingHours()).isEqualTo(workingHours);
                    });
        }

        @Test
        void active_WhenReferenceIsBeforeAnyStartDate_ThenShouldReturnEmptyOptional() {
            // Arrange
            LocalDate referenceDate = LocalDate.of(2022, 12, 31);
            Map<DayOfWeek, Duration> workingHours = createWorkingHoursMap(8);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(LocalDate.of(2023, 1, 1), workingHours)
                    )
            );

            // Act & Assert
            assertThat(regularWorkingTimes.active(referenceDate)).isEmpty();
        }

        @Test
        void active_WhenMultipleEntriesWithStartInPast_ThenShouldReturnLatestByStartDate() {
            // Arrange
            LocalDate referenceDate = LocalDate.of(2023, 8, 15);
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);
            Map<DayOfWeek, Duration> workingHours3 = createWorkingHoursMap(4);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(LocalDate.of(2023, 1, 1), workingHours1),
                            new RegularWorkingTime(LocalDate.of(2023, 7, 1), workingHours2),
                            new RegularWorkingTime(LocalDate.of(2023, 9, 1), workingHours3)
                    )
            );

            // Act & Assert
            assertThat(regularWorkingTimes.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(hours -> {
                        assertThat(hours.start()).isEqualTo(LocalDate.of(2023, 7, 1));
                        assertThat(hours.workingHours()).isEqualTo(workingHours2);
                    });
        }

        @Test
        void active_WhenEmptyList_ThenShouldReturnEmptyOptional() {
            // Arrange
            LocalDate referenceDate = LocalDate.of(2023, 8, 15);
            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(Collections.emptyList());

            // Act & Assert
            assertThat(regularWorkingTimes.active(referenceDate)).isEmpty();
        }

        @Test
        void active_WhenMultipleEntriesWithNullStartDate_ThenShouldReturnEmptyOptional() {
            // Arrange
            LocalDate referenceDate = LocalDate.of(2023, 8, 15);
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(null, workingHours1),
                            new RegularWorkingTime(null, workingHours2)
                    )
            );

            // Act & Assert
            assertThat(regularWorkingTimes.active(referenceDate)).isEmpty();
        }

        @Test
        void active_WhenReferenceDateInSameMonthButBeforeStart_ThenShouldReturnEmptyOptional() {
            // Arrange
            LocalDate referenceDate = LocalDate.of(2025, 10, 1);
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(null, workingHours1),
                            new RegularWorkingTime(LocalDate.of(2025, 10, 18), workingHours2)
                    )
            );

            // Act & Assert
            assertThat(regularWorkingTimes.active(referenceDate)).isEmpty();
        }

        @Test
        void active_WhenPayrollMonthInSameMonth_ThenShouldReturnWorkingTimes() {
            // Arrange
            YearMonth referenceDate = YearMonth.of(2025, 10);
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(null, workingHours1),
                            new RegularWorkingTime(LocalDate.of(2025, 10, 18), workingHours2)
                    )
            );

            // Act & Assert
            assertThat(regularWorkingTimes.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(hours -> {
                        assertThat(hours.start()).isEqualTo(LocalDate.of(2025, 10, 18));
                        assertThat(hours.workingHours()).isEqualTo(workingHours2);
                    });
        }
    }

    /**
     * Helper method to create a map of working hours for each day of the week
     *
     * @param hours number of hours per day
     * @return map with days of week as keys and durations as values
     */
    private Map<DayOfWeek, Duration> createWorkingHoursMap(int hours) {
        Map<DayOfWeek, Duration> workingHours = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                workingHours.put(day, Duration.ofHours(hours));
            } else {
                workingHours.put(day, Duration.ZERO);
            }
        }
        return workingHours;
    }
}
