package com.gepardec.mega.hexagon.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RegularWorkingTimesTest {

    @Nested
    @DisplayName("Tests for latest() method")
    class Latest {

        @Test
        void latest_WhenGivenMultipleRegularWorkingHours_ThenShouldReturnLatestByStartDate() {
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(LocalDate.of(2023, 1, 1), workingHours1),
                            new RegularWorkingTime(LocalDate.of(2023, 7, 1), workingHours2)
                    )
            );

            assertThat(regularWorkingTimes.latest())
                    .isPresent()
                    .hasValueSatisfying(hours -> {
                        assertThat(hours.start()).isEqualTo(LocalDate.of(2023, 7, 1));
                        assertThat(hours.workingHours()).isEqualTo(workingHours2);
                    });
        }

        @Test
        void latest_WhenGivenEmptyList_ThenShouldReturnEmptyOptional() {
            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(Collections.emptyList());

            assertThat(regularWorkingTimes.latest()).isEmpty();
        }

        @Test
        void latest_WhenAllHoursHaveSameStartDate_ThenShouldReturnAny() {
            LocalDate sameStartDate = LocalDate.of(2023, 1, 1);
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(sameStartDate, workingHours1),
                            new RegularWorkingTime(sameStartDate, workingHours2)
                    )
            );

            assertThat(regularWorkingTimes.latest())
                    .isPresent()
                    .hasValueSatisfying(hours -> assertThat(hours.start()).isEqualTo(sameStartDate));
        }
    }

    @Nested
    @DisplayName("Tests for active() method")
    class Active {

        @Test
        void active_WhenSingleEntryWithNullStartDate_ThenShouldReturnThatEntry() {
            LocalDate referenceDate = LocalDate.of(2023, 8, 15);
            Map<DayOfWeek, Duration> workingHours = createWorkingHoursMap(8);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(null, workingHours)
                    )
            );

            assertThat(regularWorkingTimes.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(hours -> {
                        assertThat(hours.start()).isNull();
                        assertThat(hours.workingHours()).isEqualTo(workingHours);
                    });
        }

        @Test
        void active_WhenMultipleEntriesIncludingOneWithNullStartDate_ThenShouldReturnLatestByStartDate() {
            LocalDate referenceDate = LocalDate.of(2023, 8, 15);
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(LocalDate.of(2023, 1, 1), workingHours1),
                            new RegularWorkingTime(null, workingHours2)
                    )
            );

            assertThat(regularWorkingTimes.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(hours -> {
                        assertThat(hours.start()).isEqualTo(LocalDate.of(2023, 1, 1));
                        assertThat(hours.workingHours()).isEqualTo(workingHours1);
                    });
        }

        @Test
        void active_WhenReferenceIsAfterStartDate_ThenShouldReturnLatestApplicableEntry() {
            LocalDate referenceDate = LocalDate.of(2023, 8, 15);
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(LocalDate.of(2023, 1, 1), workingHours1),
                            new RegularWorkingTime(LocalDate.of(2023, 7, 1), workingHours2)
                    )
            );

            assertThat(regularWorkingTimes.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(hours -> {
                        assertThat(hours.start()).isEqualTo(LocalDate.of(2023, 7, 1));
                        assertThat(hours.workingHours()).isEqualTo(workingHours2);
                    });
        }

        @Test
        void active_WhenReferenceEqualsStartDate_ThenShouldReturnThatEntry() {
            LocalDate referenceDate = LocalDate.of(2023, 7, 1);
            Map<DayOfWeek, Duration> workingHours = createWorkingHoursMap(8);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(referenceDate, workingHours)
                    )
            );

            assertThat(regularWorkingTimes.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(hours -> {
                        assertThat(hours.start()).isEqualTo(referenceDate);
                        assertThat(hours.workingHours()).isEqualTo(workingHours);
                    });
        }

        @Test
        void active_WhenReferenceIsBeforeAnyStartDate_ThenShouldReturnEmptyOptional() {
            LocalDate referenceDate = LocalDate.of(2022, 12, 31);
            Map<DayOfWeek, Duration> workingHours = createWorkingHoursMap(8);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(LocalDate.of(2023, 1, 1), workingHours)
                    )
            );

            assertThat(regularWorkingTimes.active(referenceDate)).isEmpty();
        }

        @Test
        void active_WhenMultipleEntriesWithNullStartDate_ThenShouldReturnEmptyOptional() {
            LocalDate referenceDate = LocalDate.of(2023, 8, 15);
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(null, workingHours1),
                            new RegularWorkingTime(null, workingHours2)
                    )
            );

            assertThat(regularWorkingTimes.active(referenceDate)).isEmpty();
        }

        @Test
        void active_WhenReferenceDateInSameMonthButBeforeStart_ThenShouldReturnEmptyOptional() {
            LocalDate referenceDate = LocalDate.of(2025, 10, 1);
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(null, workingHours1),
                            new RegularWorkingTime(LocalDate.of(2025, 10, 18), workingHours2)
                    )
            );

            assertThat(regularWorkingTimes.active(referenceDate)).isEmpty();
        }

        @Test
        void active_WhenPayrollMonthInSameMonth_ThenShouldReturnWorkingTimes() {
            YearMonth referenceDate = YearMonth.of(2025, 10);
            Map<DayOfWeek, Duration> workingHours1 = createWorkingHoursMap(8);
            Map<DayOfWeek, Duration> workingHours2 = createWorkingHoursMap(6);

            RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                    List.of(
                            new RegularWorkingTime(null, workingHours1),
                            new RegularWorkingTime(LocalDate.of(2025, 10, 18), workingHours2)
                    )
            );

            assertThat(regularWorkingTimes.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(hours -> {
                        assertThat(hours.start()).isEqualTo(LocalDate.of(2025, 10, 18));
                        assertThat(hours.workingHours()).isEqualTo(workingHours2);
                    });
        }
    }

    private Map<DayOfWeek, Duration> createWorkingHoursMap(int hours) {
        Map<DayOfWeek, Duration> workingHours = new EnumMap<>(DayOfWeek.class);
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
