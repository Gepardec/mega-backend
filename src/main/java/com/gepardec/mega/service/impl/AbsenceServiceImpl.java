package com.gepardec.mega.service.impl;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.service.api.AbsenceService;
import com.gepardec.mega.service.helper.OfficeCalendarUtil;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class AbsenceServiceImpl implements AbsenceService {

    @Override
    public int numberOfFridaysAbsent(List<AbsenceTime> absences, YearMonth payrollMonth) {
        return (int) absentWorkingDaysInMonth(absences, payrollMonth).stream()
                .filter(OfficeCalendarUtil::isFriday)
                .count();
    }

    @Override
    public int getNumberOfDaysAbsent(List<AbsenceTime> absences, YearMonth payrollMonth) {
        return absentWorkingDaysInMonth(absences, payrollMonth).size();
    }

    // Absences may extend beyond the month and may overlap each other, so trim them
    // to the month and count each working day only once.
    private Set<LocalDate> absentWorkingDaysInMonth(List<AbsenceTime> absences, YearMonth payrollMonth) {
        LocalDate monthStart = payrollMonth.atDay(1);
        LocalDate monthEnd = payrollMonth.atEndOfMonth();

        return absences.stream()
                .flatMap(absence -> {
                    LocalDate from = absence.fromDate().isBefore(monthStart) ? monthStart : absence.fromDate();
                    LocalDate to = absence.toDate().isAfter(monthEnd) ? monthEnd : absence.toDate();
                    return from.isAfter(to)
                            ? Stream.empty()
                            : OfficeCalendarUtil.getWorkingDaysBetween(from, to).stream();
                })
                .collect(Collectors.toSet());
    }
}
