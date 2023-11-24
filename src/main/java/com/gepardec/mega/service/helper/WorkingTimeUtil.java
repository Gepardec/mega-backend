package com.gepardec.mega.service.helper;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.service.impl.MonthlyReportServiceImpl;
import de.provantis.zep.FehlzeitType;
import de.provantis.zep.ProjektzeitType;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil.getWorkingDaysBetween;

@ApplicationScoped
public class WorkingTimeUtil {

    private static final String BILLABLE_TIME_FORMAT = "HH:mm";
    private static final List<String> BOOKABLE_ABSENCES = List.of(
            MonthlyReportServiceImpl.CONFERENCE_DAYS,
            MonthlyReportServiceImpl.EXTERNAL_TRAINING_DAYS,
            MonthlyReportServiceImpl.HOME_OFFICE_DAYS
    );


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

    public double getOvertimeForEmployee(Employee employee,
                                         List<ProjektzeitType> billableEntries,
                                         List<FehlzeitType> fehlzeitTypeList,
                                         LocalDate date) {
        if (employee.getRegularWorkingHours() == null) {
            return 0.0;
        }

        // In case there are absences that do not affect the current month, filter them out
        fehlzeitTypeList = fehlzeitTypeList.stream()
                .filter(ftl -> DateUtils.parseDate(ftl.getStartdatum()).getMonthValue() == date.getMonthValue())
                .collect(Collectors.toList());

        //FIXME
        var workingDaysCountMap = getWorkingDaysBetween(date, DateUtils.getLastDayOfCurrentMonth(date.toString()))
                .stream()
                .collect(Collectors.groupingBy(LocalDate::getDayOfWeek, Collectors.counting()));
        var absenceDaysCountMap = getAbsenceDaysCountMap(fehlzeitTypeList, date);
        var presentDaysCountMap = workingDaysCountMap.entrySet().stream()
                .map(entry -> removeAbsenceDays(entry, absenceDaysCountMap))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        var monthlyRegularWorkingHours = presentDaysCountMap.entrySet().stream()
                .map(entry -> employee.getRegularWorkingHours().get(entry.getKey()).multipliedBy(entry.getValue()))
                .reduce(Duration::plus)
                .orElse(Duration.ZERO);

        Duration totalWorkingHours = getWorkingTimesForEmployee(billableEntries, employee, $ -> true);
        Duration overtime = totalWorkingHours.minus(monthlyRegularWorkingHours);

        return (double) overtime.toMinutes() / 60;
    }

    private static Map.Entry<DayOfWeek, Long> removeAbsenceDays(Map.Entry<DayOfWeek, Long> workingDayEntry,
                                                                Map<DayOfWeek, Long> absenceDaysCountMap) {
        return Map.entry(
                workingDayEntry.getKey(),
                workingDayEntry.getValue() - Optional.ofNullable(absenceDaysCountMap.get(workingDayEntry.getKey())).orElse(0L)
        );
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
                        getWorkingDaysBetween(
                                LocalDate.parse(ftl.getStartdatum()),
                                LocalDate.parse(ftl.getEnddatum())
                        ).size()
                )
                .sum();
    }

    public Map<DayOfWeek, Long> getAbsenceDaysCountMap(@Nonnull List<FehlzeitType> fehlZeitTypeList, LocalDate date) {
        return fehlZeitTypeList.stream()
                .filter(ftl -> !BOOKABLE_ABSENCES.contains(ftl.getFehlgrund()))
                .filter(FehlzeitType::isGenehmigt)
                .map(fehlzeitType -> trimDurationToCurrentMonth(fehlzeitType, date))
                .map(ftl -> getWorkingDaysBetween(LocalDate.parse(ftl.getStartdatum()), LocalDate.parse(ftl.getEnddatum())))
                .flatMap(this::getDayOfWeeks)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private Stream<DayOfWeek> getDayOfWeeks(List<LocalDate> dates) {
        return dates.stream().map(LocalDate::getDayOfWeek);
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
