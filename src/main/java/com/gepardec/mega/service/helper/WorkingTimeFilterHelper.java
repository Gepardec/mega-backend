package com.gepardec.mega.service.helper;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil;
import de.provantis.zep.FehlzeitType;
import de.provantis.zep.ProjektzeitType;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.function.Predicate;

@ApplicationScoped
public class WorkingTimeFilterHelper {

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

    public double getOvertimeforEmployee(Employee employee, List<ProjektzeitType> billableEntries) {
        if (employee.getRegularWorkingHours() == null) {
            return 0.0;
        }

        Duration weeklyRegularWorkingHours = employee.getRegularWorkingHours().values().stream().reduce(Duration::plus).orElse(Duration.ZERO).multipliedBy(4);
        Duration totalWorkingHours = getWorkingTimesForEmployee(billableEntries, employee, $ -> true);
        Duration overtime = totalWorkingHours.minus(weeklyRegularWorkingHours);

        return (double) overtime.toMinutes() / 60;
    }

    private Duration getWorkingTimesForEmployee(List<ProjektzeitType> projektzeitTypeList, Employee employee, Predicate<ProjektzeitType> billableFilter) {
        return projektzeitTypeList.stream()
                .filter(pzt -> pzt.getUserId().equals(employee.getUserId()))
                .filter(billableFilter)
                .map(pzt -> LocalTime.parse(pzt.getDauer()))
                .map(lt -> Duration.between(LocalTime.MIN, lt))
                .reduce(Duration.ZERO, Duration::plus);
    }


    // Calculator functions for FehlzeitType

    public int getAbsenceTimesForEmployee(@Nonnull List<FehlzeitType> fehlZeitTypeList, String absenceType, LocalDate date) {
        System.out.println(fehlZeitTypeList);
        return (int) fehlZeitTypeList.stream()
                .filter(fzt -> fzt.getFehlgrund().equals(absenceType))
                .filter(FehlzeitType::isGenehmigt)
                .map(fehlzeitType -> trimDurationToCurrentMonth(fehlzeitType, date))
                .mapToLong(ftl -> OfficeCalendarUtil.getWorkingDaysBetween(LocalDate.parse(ftl.getStartdatum()), LocalDate.parse(ftl.getEnddatum())).size())
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
