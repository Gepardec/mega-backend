package com.gepardec.mega.service.api;

import com.gepardec.mega.domain.model.Employee;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.time.YearMonth;

public interface DateHelperService {
    // dependent from month confirmation, returns Pair<fromDateString, toDateString>
    Pair<String, String> getCorrectDateForRequest(Employee employee, YearMonth yearMonth);
    int getNumberOfFridaysInMonth(LocalDate fromDate);
    int getFridaysInRange(LocalDate fromDate, LocalDate toDate);
    int getNumberOfWorkingDaysForMonthWithoutHolidays(LocalDate fromDate);
}
