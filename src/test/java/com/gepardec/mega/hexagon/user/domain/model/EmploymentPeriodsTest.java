package com.gepardec.mega.hexagon.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmploymentPeriodsTest {

    @Test
    void constructor_WhenPeriodsHaveDifferentOrder_ThenCanonicalizesForStableEquality() {
        EmploymentPeriod early = new EmploymentPeriod(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 6, 30));
        EmploymentPeriod late = new EmploymentPeriod(LocalDate.of(2023, 7, 1), null);

        EmploymentPeriods fromSource = new EmploymentPeriods(List.of(late, early));
        EmploymentPeriods fromPersistence = new EmploymentPeriods(List.of(early, late));

        assertThat(fromSource).isEqualTo(fromPersistence);
        assertThat(fromSource.employmentPeriods()).containsExactly(early, late);
    }

    @Nested
    @DisplayName("Tests for latest() method")
    class Latest {

        @Test
        void latest_WhenGivenMultipleEmploymentPeriods_ThenShouldReturnLatestByStartDate() {
            EmploymentPeriods employmentPeriods = new EmploymentPeriods(
                    List.of(
                            new EmploymentPeriod(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 6, 30)),
                            new EmploymentPeriod(LocalDate.of(2023, 7, 1), null)
                    )
            );

            assertThat(employmentPeriods.latest())
                    .isPresent()
                    .hasValueSatisfying(period -> {
                        assertThat(period.start()).isEqualTo(LocalDate.of(2023, 7, 1));
                        assertThat(period.end()).isNull();
                    });
        }

        @Test
        void latest_WhenGivenEmptyList_ThenShouldReturnEmptyOptional() {
            EmploymentPeriods employmentPeriods = new EmploymentPeriods(Collections.emptyList());

            assertThat(employmentPeriods.latest()).isEmpty();
        }

        @Test
        void latest_WhenAllPeriodsHaveSameStartDate_ThenShouldReturnAny() {
            LocalDate sameStartDate = LocalDate.of(2023, 1, 1);
            EmploymentPeriods employmentPeriods = new EmploymentPeriods(
                    List.of(
                            new EmploymentPeriod(sameStartDate, LocalDate.of(2023, 6, 30)),
                            new EmploymentPeriod(sameStartDate, null)
                    )
            );

            assertThat(employmentPeriods.latest())
                    .isPresent()
                    .hasValueSatisfying(period -> assertThat(period.start()).isEqualTo(sameStartDate));
        }
    }

    @Nested
    @DisplayName("Tests for active() method with LocalDate")
    class Active_LocalDate {

        @Test
        void active_WhenReferenceIsWithinActivePeriod_ThenShouldReturnActivePeriod() {
            LocalDate referenceDate = LocalDate.of(2023, 8, 15);
            EmploymentPeriods employmentPeriods = new EmploymentPeriods(
                    List.of(
                            new EmploymentPeriod(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 6, 30)),
                            new EmploymentPeriod(LocalDate.of(2023, 7, 1), LocalDate.of(2023, 12, 31))
                    )
            );

            assertThat(employmentPeriods.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(period -> {
                        assertThat(period.start()).isEqualTo(LocalDate.of(2023, 7, 1));
                        assertThat(period.end()).isEqualTo(LocalDate.of(2023, 12, 31));
                    });
        }

        @Test
        void active_WhenReferenceIsWithinOpenEndedPeriod_ThenShouldReturnOpenEndedPeriod() {
            LocalDate referenceDate = LocalDate.of(2023, 8, 15);
            EmploymentPeriods employmentPeriods = new EmploymentPeriods(
                    List.of(
                            new EmploymentPeriod(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 6, 30)),
                            new EmploymentPeriod(LocalDate.of(2023, 7, 1), null)
                    )
            );

            assertThat(employmentPeriods.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(period -> {
                        assertThat(period.start()).isEqualTo(LocalDate.of(2023, 7, 1));
                        assertThat(period.end()).isNull();
                    });
        }

        @Test
        void active_WhenReferenceIsBeforeAnyPeriod_ThenShouldReturnEmptyOptional() {
            LocalDate referenceDate = LocalDate.of(2022, 12, 31);
            EmploymentPeriods employmentPeriods = new EmploymentPeriods(
                    List.of(
                            new EmploymentPeriod(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 6, 30)),
                            new EmploymentPeriod(LocalDate.of(2023, 7, 1), null)
                    )
            );

            assertThat(employmentPeriods.active(referenceDate)).isEmpty();
        }

        @Test
        void active_WhenReferenceIsAfterClosedPeriod_ThenShouldReturnEmptyOptional() {
            LocalDate referenceDate = LocalDate.of(2023, 7, 1);
            EmploymentPeriods employmentPeriods = new EmploymentPeriods(
                    List.of(
                            new EmploymentPeriod(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 6, 30))
                    )
            );

            assertThat(employmentPeriods.active(referenceDate)).isEmpty();
        }

        @Test
        void active_WhenReferenceEqualsStartDate_ThenShouldReturnPeriod() {
            LocalDate referenceDate = LocalDate.of(2023, 1, 1);
            EmploymentPeriods employmentPeriods = new EmploymentPeriods(
                    List.of(
                            new EmploymentPeriod(referenceDate, LocalDate.of(2023, 6, 30))
                    )
            );

            assertThat(employmentPeriods.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(period -> assertThat(period.start()).isEqualTo(referenceDate));
        }

        @Test
        void active_WhenReferenceEqualsEndDate_ThenShouldReturnPeriod() {
            LocalDate referenceDate = LocalDate.of(2023, 6, 30);
            EmploymentPeriods employmentPeriods = new EmploymentPeriods(
                    List.of(
                            new EmploymentPeriod(LocalDate.of(2023, 1, 1), referenceDate)
                    )
            );

            assertThat(employmentPeriods.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(period -> assertThat(period.end()).isEqualTo(referenceDate));
        }

        @Test
        void active_WhenPeriodStartIsNull_ThenShouldReturnEmptyOptional() {
            LocalDate referenceDate = LocalDate.of(2023, 8, 15);
            EmploymentPeriods employmentPeriods = new EmploymentPeriods(
                    List.of(
                            new EmploymentPeriod(null, LocalDate.of(2023, 12, 31))
                    )
            );

            assertThat(employmentPeriods.active(referenceDate)).isEmpty();
        }

        @Test
        void active_WhenMultipleActivePeriods_ThenShouldReturnLatestByStartDate() {
            LocalDate referenceDate = LocalDate.of(2023, 8, 15);
            EmploymentPeriods employmentPeriods = new EmploymentPeriods(
                    List.of(
                            new EmploymentPeriod(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31)),
                            new EmploymentPeriod(LocalDate.of(2023, 7, 1), LocalDate.of(2023, 9, 30))
                    )
            );

            assertThat(employmentPeriods.active(referenceDate))
                    .isPresent()
                    .hasValueSatisfying(period -> assertThat(period.start()).isEqualTo(LocalDate.of(2023, 7, 1)));
        }
    }

    @Nested
    @DisplayName("Tests for active() method with YearMonth")
    class Active_YearMonth {

        @ParameterizedTest
        @CsvSource(
                {
                        //periods with end date
                        "2023-06,2023-01-01,2023-06-01,true",
                        "2023-06,2023-01-01,2023-06-15,true",
                        "2023-06,2023-01-01,2023-06-30,true",
                        "2023-06,2023-01-01,2023-07-01,true",
                        "2023-06,2023-01-01,2023-05-30,false",

                        // open-ended periods
                        "2023-06,2023-06-01,,true",
                        "2023-06,2023-06-15,,true",
                        "2023-06,2023-06-30,,true",
                        "2023-06,2023-05-30,,true",
                        "2023-06,2023-07-01,,false",
                }
        )
        void active_YearMonth(YearMonth payrollMonth, LocalDate startDate, LocalDate endDate, boolean isActive) {
            EmploymentPeriods periods = new EmploymentPeriods(new EmploymentPeriod(startDate, endDate));

            boolean active = periods.active(payrollMonth).isPresent();

            assertThat(active).isEqualTo(isActive);
        }
    }
}
