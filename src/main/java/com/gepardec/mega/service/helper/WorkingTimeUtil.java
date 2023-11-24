package com.gepardec.mega.service.helper;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil;
import de.provantis.zep.FehlzeitType;
import de.provantis.zep.ProjektzeitType;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApplicationScoped
public class WorkingTimeUtil {

    private static final String BILLABLE_TIME_FORMAT = "HH:mm";


    // Calculator functions for ProjektzeitType

    public String getInternalTimesForEmployee(@Nonnull List<ProjektzeitType> projektzeitTypeList, @Nonnull Employee employee) {
        Duration internalTimesForEmployee = getWorkingTimesForEmployee(projektzeitTypeList, employee, Predicate.not(ProjektzeitType::isIstFakturierbar));
        return DurationFormatUtils.formatDuration(internalTimesForEmployee.toMillis(), BILLABLE_TIME_FORMAT);
    }

    public String getBillableTimesForEmployee(@Nonnull List<ProjektzeitType> projektzeitTypeList, @Nonnull Employee employee) {
        Duration billableTimesForEmployee = getWorkingTimesForEmployee(projektzeitTypeList, employee, ProjektzeitType::isIstFakturierbar);
        return DurationFormatUtils.formatDuration(billableTimesForEmployee.toMillis(), BILLABLE_TIME_FORMAT);

    }

    public String getTotalWorkingTimeForEmployee(@Nonnull List<ProjektzeitType> projektzeitTypeList, @Nonnull Employee employee) {
        Duration totalWorkingTimeForEmployee = getWorkingTimesForEmployee(projektzeitTypeList, employee, $ -> true);
        return DurationFormatUtils.formatDuration(totalWorkingTimeForEmployee.toMillis(), BILLABLE_TIME_FORMAT);
    }

    public double getOvertimeforEmployee(Employee employee, List<ProjektzeitType> billableEntries, List<FehlzeitType> fehlzeitTypeList, LocalDate date) {
        if (employee.getRegularWorkingHours() == null) {
            return 0.0;
        }

        YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonth());
        int lengthOfMonth = yearMonth.lengthOfMonth();
        int excessDays = lengthOfMonth - (7 * 4);
        LocalDate firstday = yearMonth.atDay(1);

        //FIXME
//      Calculate regular working hours with hinsight of different month lengths
        Map<DayOfWeek, Duration> regularWorkingHours = employee.getRegularWorkingHours();
        Duration workingHoursInMonth = regularWorkingHours.values()
                .stream()
                .reduce(Duration::plus)
                .orElse(Duration.ZERO)
                .multipliedBy(4);
        for (int i = 0; i < excessDays; i++) {
            Duration durationOfExcessDay = regularWorkingHours.get(firstday.getDayOfWeek());
            workingHoursInMonth = workingHoursInMonth.plus(durationOfExcessDay);
            firstday = firstday.plusDays(1);
        }

//      Remove holidays from regular working time in month
        List<DayOfWeek> holidayDays = OfficeCalendarUtil.getHolidaysForMonth(yearMonth)
                .map(LocalDate::getDayOfWeek)
                .collect(Collectors.toList());

        for (DayOfWeek dayOfWeek : holidayDays) {
            Duration holidayWorkingDuration = regularWorkingHours.get(dayOfWeek);
            workingHoursInMonth = workingHoursInMonth.minus(holidayWorkingDuration);
        }

//      Remove fehlzeit days from regular working time in month
        List<DayOfWeek> fehlzeitDays = fehlzeitTypeList.stream().map(fehlzeitType -> {
            LocalDate startdate = LocalDate.parse(fehlzeitType.getStartdatum());
            LocalDate enddate = LocalDate.parse(fehlzeitType.getEnddatum());

            if (startdate.getMonth().getValue() < enddate.getMonth().getValue()) {
                enddate = startdate.withDayOfMonth(startdate.lengthOfMonth());
            }
            enddate = enddate.plusDays(1);

            return startdate.datesUntil(enddate)
                    .map(LocalDate::getDayOfWeek)
                    .collect(Collectors.toList());
        }).flatMap(List::stream).collect(Collectors.toList());

        for (DayOfWeek dayOfWeek : fehlzeitDays) {
            Duration holidayWorkingDuration = regularWorkingHours.get(dayOfWeek);
            workingHoursInMonth = workingHoursInMonth.minus(holidayWorkingDuration);
        }

        if (workingHoursInMonth.isNegative()) {
            workingHoursInMonth = Duration.ZERO;
        }

        Duration totalWorkingHours = getWorkingTimesForEmployee(billableEntries, employee, $ -> true);
        Duration overtime = totalWorkingHours.minus(workingHoursInMonth);

        return (double) overtime.toMinutes() / 60;
    }

    private Duration getWorkingTimesForEmployee(List<ProjektzeitType> projektzeitTypeList,
                                                Employee employee,
                                                Predicate<ProjektzeitType> billableFilter) {
        return projektzeitTypeList.stream()
                .filter(pzt -> pzt.getUserId().equals(employee.getUserId()))
                .filter(billableFilter)
                .map(pzt -> LocalTime.parse(pzt.getDauer()))
                .map(lt -> Duration.between(LocalTime.MIN, lt))
                .reduce(Duration.ZERO, Duration::plus);
    }

    // Calculator functions for FehlzeitType

    public int getAbsenceTimesForEmployee(@Nonnull List<FehlzeitType> fehlZeitTypeList, String absenceType, LocalDate date) {
        return (int) fehlZeitTypeList.stream()
                .filter(fzt -> fzt.getFehlgrund().equals(absenceType))
                .filter(FehlzeitType::isGenehmigt)
                .map(fehlzeitType -> trimDurationToCurrentMonth(fehlzeitType, date))
                .mapToLong(ftl ->
                        OfficeCalendarUtil.getWorkingDaysBetween(
                                LocalDate.parse(ftl.getStartdatum()),
                                LocalDate.parse(ftl.getEnddatum())
                        ).size()
                )
                .sum();
    }

    private FehlzeitType trimDurationToCurrentMonth(FehlzeitType fehlzeit, LocalDate date) {
        if (LocalDate.parse(fehlzeit.getEnddatum()).getMonthValue() > date.getMonthValue()) {
            fehlzeit.setEnddatum(date.with(TemporalAdjusters.lastDayOfMonth()).toString());
        }
        if (LocalDate.parse(fehlzeit.getStartdatum()).getMonthValue() < date.getMonthValue()) {
            fehlzeit.setStartdatum(date.with(TemporalAdjusters.firstDayOfMonth()).toString());
        }
        return fehlzeit;
    }
}
