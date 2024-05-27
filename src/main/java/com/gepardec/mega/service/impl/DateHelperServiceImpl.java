package com.gepardec.mega.service.impl;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.service.api.DateHelperService;
import com.gepardec.mega.service.api.MonthlyReportService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.time.YearMonth;

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
