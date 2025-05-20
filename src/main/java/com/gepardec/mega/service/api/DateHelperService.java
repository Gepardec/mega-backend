package com.gepardec.mega.service.api;

import java.time.LocalDate;
import java.time.YearMonth;

public interface DateHelperService {

    int getNumberOfFridaysInMonth(YearMonth payrollMonth);

    int getFridaysInRange(LocalDate fromDate, LocalDate toDate);

    int getNumberOfWorkingDaysForMonthWithoutHolidays(YearMonth payrollMonth);
}
