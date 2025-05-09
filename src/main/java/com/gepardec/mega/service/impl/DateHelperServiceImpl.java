package com.gepardec.mega.service.impl;

import com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil;
import com.gepardec.mega.service.api.DateHelperService;
import jakarta.enterprise.context.RequestScoped;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Predicate;

@RequestScoped
public class DateHelperServiceImpl implements DateHelperService {

    @Override
    public int getNumberOfFridaysInMonth(YearMonth payrollMonth) {
        return getFridaysInRange(payrollMonth.atDay(1), payrollMonth.atEndOfMonth());
    }

    @Override
    public int getFridaysInRange(LocalDate fromDate, LocalDate toDateInclusive) {
        Predicate<LocalDate> isFriday = OfficeCalendarUtil::isFriday;
        Predicate<LocalDate> isWorkingDay = OfficeCalendarUtil::isWorkingDay;

        return (int) fromDate.datesUntil(toDateInclusive.plusDays(1))
                .filter(isFriday.and(isWorkingDay))
                .count();
    }

    @Override
    public int getNumberOfWorkingDaysForMonthWithoutHolidays(YearMonth payrollMonth) {
        return OfficeCalendarUtil.getWorkingDaysForYearMonth(payrollMonth).size();
    }
}
