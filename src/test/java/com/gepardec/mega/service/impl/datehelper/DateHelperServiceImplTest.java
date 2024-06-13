package com.gepardec.mega.service.impl.datehelper;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.service.api.DateHelperService;

import com.gepardec.mega.service.api.MonthlyReportService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@QuarkusTest
class DateHelperServiceImplTest {

    @Inject
    DateHelperService dateHelperService;


    @InjectMock
    MonthlyReportService monthlyReportService;

    @Test
    void testGetNumberOfFridaysInMonth_whenMonthIsNovember2024_thenReturnFour(){
        int actual = dateHelperService.getNumberOfFridaysInMonth(LocalDate.of(2024, 11,1));
        assertThat(actual).isEqualTo(4);
    }

    @Test
    void testGetNumberOfFridaysInRange_whenRangeContainsTwo_thenReturnTwo(){
        int actual = dateHelperService.getFridaysInRange(LocalDate.of(2024, 11,7), LocalDate.of(2024, 11, 15));
        assertThat(actual).isEqualTo(2);
    }

    @Test
    void testGetNumberOfWorkingDaysForMonthWithoutHolidays_whenMonthIsApril2024_thenReturnTwentyOne() {
        int actual = dateHelperService.getNumberOfWorkingDaysForMonthWithoutHolidays(LocalDate.of(2024, 4, 1));
        assertThat(actual).isEqualTo(21);
    }

    @Test
    void getCorrectDateForRequest_whenYearMonthProvided_thenReturnCorrectDate(){
        LocalDate mockCurrentDate = LocalDate.of(2024,5,15);
        LocalDate previousMonth = mockCurrentDate.minusMonths(1);
        LocalDate firstOfPreviousMonth = previousMonth.withDayOfMonth(1);
        LocalDate mockMidOfMonth = mockCurrentDate.withDayOfMonth(14);

        LocalDate firstOfCurrentMonth = LocalDate.of(2024, 5, 1);
        LocalDate lastOfCurrentMonth = LocalDate.of(2024, 5, 31);
        YearMonth yearMonthMock = YearMonth.of(2024, 5);

        String expectedFrom = "2024-05-01";
        String expectedTo = "2024-05-31";


        try (MockedStatic<LocalDate> mockedStatic = mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(mockCurrentDate);
            mockedStatic.when(() -> LocalDate.now().minusMonths(1)).thenReturn(previousMonth);
            mockedStatic.when(() -> LocalDate.now().withMonth(LocalDate.now().getMonth().minus(1).getValue()).withDayOfMonth(1)).thenReturn(firstOfPreviousMonth);
            mockedStatic.when(() -> LocalDate.now().withDayOfMonth(14)).thenReturn(mockMidOfMonth);

            try(MockedStatic<DateUtils> dateUtils = mockStatic(DateUtils.class)) {
                try (MockedStatic<YearMonth> yearMonth = mockStatic(YearMonth.class)) {

                    yearMonth.when(() -> YearMonth.of(2024, 5)).thenReturn(yearMonthMock);
                    yearMonth.when(() -> yearMonthMock.atDay(1)).thenReturn(firstOfCurrentMonth);
                }

                    dateUtils.when(() -> DateUtils.getLastDayOfCurrentMonth(anyString()))
                            .thenReturn(lastOfCurrentMonth);

                    dateUtils.when(() -> DateUtils.formatDate(firstOfCurrentMonth))
                            .thenReturn(expectedFrom);

                    dateUtils.when(() -> DateUtils.formatDate(lastOfCurrentMonth))
                            .thenReturn(expectedTo);
                }
            }

            Pair<String, String> actual = dateHelperService.getCorrectDateForRequest(Employee.builder().userId("007-jbond").build(), YearMonth.of(2024,5));

            assertThat(actual).isNotNull();
            assertThat(actual.getLeft()).isEqualTo(expectedFrom);
            assertThat(actual.getRight()).isEqualTo(expectedTo);
        }

    @Test
    void getCorrectDateForRequest_whenYearMonthIsNotProvidedAndNowIsAfterMidOfMonthAndConfirmed_thenReturnCorrectDate(){
        LocalDate mockCurrentDate = LocalDate.of(2024,5,15);
        LocalDate previousMonth = mockCurrentDate.minusMonths(1);
        LocalDate firstOfPreviousMonth = previousMonth.withDayOfMonth(1);
        LocalDate mockMidOfMonth = mockCurrentDate.withDayOfMonth(14);

        String expectedFrom = "2024-05-01";
        String expectedTo = "2024-05-31";

        try(MockedStatic<LocalDate> mockedStatic = mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalDate::now).thenReturn(mockCurrentDate).thenReturn(mockCurrentDate);
            mockedStatic.when(() -> LocalDate.now().minusMonths(1)).thenReturn(previousMonth);
            mockedStatic.when(() -> LocalDate.now().withMonth(LocalDate.now().getMonth().minus(1).getValue()).withDayOfMonth(1)).thenReturn(firstOfPreviousMonth);
            mockedStatic.when(() -> LocalDate.now().withDayOfMonth(14)).thenReturn(mockMidOfMonth);

            try(MockedStatic<DateUtils> dateUtils = mockStatic(DateUtils.class)) {
                dateUtils.when(() -> DateUtils.getFirstDayOfCurrentMonth(any(LocalDate.class)))
                        .thenReturn(expectedFrom);

                dateUtils.when(() -> DateUtils.getLastDayOfCurrentMonth(any(LocalDate.class)))
                        .thenReturn(expectedTo);

                when(monthlyReportService.isMonthConfirmedFromEmployee(any(Employee.class), any(LocalDate.class)))
                        .thenReturn(true);

                Pair<String, String> actual = dateHelperService.getCorrectDateForRequest(Employee.builder().userId("007-jbond").build(), null);
                assertThat(actual.getLeft()).isEqualTo(expectedFrom);
            }
        }
    }

    @Test
    void getCorrectDateForRequest_whenYearMonthIsNotProvidedAndNowIsAfterMidOfMonthAndNotConfirmed_thenReturnCorrectDate(){
        LocalDate mockCurrentDate = LocalDate.of(2024,5,15);
        LocalDate previousMonth = mockCurrentDate.minusMonths(1);
        LocalDate firstOfPreviousMonth = previousMonth.withDayOfMonth(1);
        LocalDate mockMidOfMonth = mockCurrentDate.withDayOfMonth(14);


        LocalDate lastOfPreviousMonth = LocalDate.of(2024, 4, 30);

        String expectedFrom = "2024-04-01";
        String expectedTo = "2024-04-30";

        try(MockedStatic<LocalDate> mockedStatic = mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalDate::now).thenReturn(mockCurrentDate).thenReturn(mockCurrentDate);
            mockedStatic.when(() -> LocalDate.now().minusMonths(1)).thenReturn(previousMonth);
            mockedStatic.when(() -> LocalDate.now().withMonth(LocalDate.now().getMonth().minus(1).getValue()).withDayOfMonth(1)).thenReturn(firstOfPreviousMonth);
            mockedStatic.when(() -> LocalDate.now().withDayOfMonth(14)).thenReturn(mockMidOfMonth);

            try(MockedStatic<DateUtils> dateUtils = mockStatic(DateUtils.class)) {

                dateUtils.when(() -> DateUtils.getLastDayOfCurrentMonth(any(LocalDate.class)))
                        .thenReturn(expectedTo);

                dateUtils.when(() -> DateUtils.formatDate(firstOfPreviousMonth))
                        .thenReturn(expectedFrom);

                dateUtils.when(() -> DateUtils.formatDate(lastOfPreviousMonth))
                        .thenReturn(expectedTo);

                when(monthlyReportService.isMonthConfirmedFromEmployee(any(Employee.class), any(LocalDate.class)))
                        .thenReturn(false);

                Pair<String, String> actual = dateHelperService.getCorrectDateForRequest(Employee.builder().userId("007-jbond").build(), null);
                assertThat(actual.getLeft()).isEqualTo(expectedFrom);
            }
        }
    }
}



