package com.gepardec.mega.service.impl;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil;
import com.gepardec.mega.service.api.AbsenceService;
import com.gepardec.mega.service.api.DateHelperService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
public class AbsenceServiceImpl implements AbsenceService {

    @Inject
    DateHelperService dateHelperService;

    @Override
    public int numberOfFridaysAbsent(List<AbsenceTime> absences) {
        return absences.stream()
                .mapToInt(absence -> dateHelperService.getFridaysInRange(absence.fromDate(), absence.toDate()))
                .sum();
    }

    @Override
    public int getNumberOfDaysAbsent(List<AbsenceTime> absences, YearMonth payrollMonth) {
        List<LocalDate> holidays = OfficeCalendarUtil.getHolidaysForMonth(YearMonth.of(payrollMonth.getYear(), payrollMonth.getMonth())).toList();

        int count = 0;
        for (AbsenceTime a : absences) {
            LocalDate fromDate = a.fromDate();
            LocalDate toDate = a.toDate();

            if (fromDate.equals(toDate)) {
                if (!holidays.contains(fromDate) && OfficeCalendarUtil.isWorkingDay(fromDate)) {
                    count += 1;
                }
                continue;
            }
            for (LocalDate currentDate = fromDate; !currentDate.isAfter(toDate); currentDate = currentDate.plusDays(1)) {
                if (!holidays.contains(currentDate) && OfficeCalendarUtil.isWorkingDay(currentDate)) {
                    count += 1;
                }
            }
        }
        return count;
    }
}
