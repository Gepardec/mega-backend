package com.gepardec.mega.service.impl;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil;
import com.gepardec.mega.service.api.DateHelperService;
import com.gepardec.mega.service.api.MonthlyReportService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static com.gepardec.mega.domain.utils.DateUtils.formatDate;
import static com.gepardec.mega.domain.utils.DateUtils.getFirstDayOfCurrentMonth;
import static com.gepardec.mega.domain.utils.DateUtils.getLastDayOfCurrentMonth;

@RequestScoped
public class DateHelperServiceImpl implements DateHelperService {
    @Inject
    MonthlyReportService monthlyReportService;

    @Override
    public Pair<String, String> getCorrectDateForRequest(Employee employee, YearMonth yearMonth) {
        LocalDate now = LocalDate.now();
        LocalDate firstOfPreviousMonth = now.withMonth(now.getMonth().minus(1).getValue()).withDayOfMonth(1);
        LocalDate midOfCurrentMonth = LocalDate.now().withDayOfMonth(14);

        if (yearMonth != null) {
            return getDateWhenYearMonthProvided(yearMonth);
        }
        if (now.isAfter(midOfCurrentMonth) && monthlyReportService.isMonthConfirmedFromEmployee(employee, firstOfPreviousMonth)) {
            return getDateWhenMonthIsConfirmedFromEmployeeAndMidOfMonthIsReached();
        }

        String fromDate = formatDate(firstOfPreviousMonth);
        String toDate = formatDate(getLastDayOfCurrentMonth(fromDate));
        return Pair.of(fromDate,toDate);
    }

    @Override
    public int getNumberOfFridaysInMonth(LocalDate fromDate) {
        LocalDate toDate = DateUtils.getLastDayOfCurrentMonth(fromDate.toString());
        int fridayCounter = 0;

        for (LocalDate day = fromDate; !day.isAfter(toDate); day = day.plusDays(1)) {
            if(OfficeCalendarUtil.isFriday(day) && OfficeCalendarUtil.isWorkingDay(day)){
                fridayCounter++;
            }
        }
        return fridayCounter;
    }

    @Override
    public int getFridaysInRange(LocalDate fromDate, LocalDate toDate) {
        int count = 0;

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            if (OfficeCalendarUtil.isFriday(date) && OfficeCalendarUtil.isWorkingDay(date)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int getNumberOfWorkingDaysForMonthWithoutHolidays(LocalDate fromDate) {
        LocalDate toDate = DateUtils.getLastDayOfCurrentMonth(fromDate.toString());
        int totalNumberOfDaysInMonth = toDate.getDayOfMonth();
        List<LocalDate> holidays = OfficeCalendarUtil.getHolidaysForMonth(YearMonth.of(fromDate.getYear(), fromDate.getMonth())).toList();
        int count = 0;

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            if(holidays.contains(date)){
                count++;
            } else if (!OfficeCalendarUtil.isWorkingDay(date)) {
                count++;
            }
        }
        return (totalNumberOfDaysInMonth - count);
    }

    private Pair<String, String> getDateWhenYearMonthProvided(YearMonth yearMonth) {
        String fromDate = formatDate(yearMonth.atDay(1));
        String toDate = formatDate(getLastDayOfCurrentMonth(fromDate));
        return Pair.of(fromDate, toDate);
    }

    private Pair<String, String> getDateWhenMonthIsConfirmedFromEmployeeAndMidOfMonthIsReached() {
        LocalDate now = LocalDate.now();
        String fromDate = getFirstDayOfCurrentMonth(now);
        String toDate = getLastDayOfCurrentMonth(now);
        return Pair.of(fromDate, toDate);
    }
}
