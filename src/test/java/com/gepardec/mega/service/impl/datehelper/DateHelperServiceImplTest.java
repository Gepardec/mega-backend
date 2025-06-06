package com.gepardec.mega.service.impl.datehelper;

import com.gepardec.mega.service.api.DateHelperService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@QuarkusTest
class DateHelperServiceImplTest {

    @Inject
    DateHelperService dateHelperService;

    @Test
    void testGetNumberOfFridaysInMonth_whenMonthIsNovember2024_thenReturnFour() {
        int actual = dateHelperService.getNumberOfFridaysInMonth(YearMonth.of(2024, 11));
        assertThat(actual).isEqualTo(4);
    }

    @Test
    void testGetNumberOfFridaysInRange_whenRangeContainsTwo_thenReturnTwo() {
        int actual = dateHelperService.getFridaysInRange(LocalDate.of(2024, 11, 7), LocalDate.of(2024, 11, 15));
        assertThat(actual).isEqualTo(2);
    }

    @Test
    void testGetNumberOfWorkingDaysForMonthWithoutHolidays_whenMonthIsApril2024_thenReturnTwentyOne() {
        int actual = dateHelperService.getNumberOfWorkingDaysForMonthWithoutHolidays(YearMonth.of(2024, 4));
        assertThat(actual).isEqualTo(21);
    }
}
