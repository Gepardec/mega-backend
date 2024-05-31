package com.gepardec.mega.service.helper;

import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.utils.DateUtils;
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
            AbsenceType.CONFERENCE_DAYS.getAbsenceName(),
            AbsenceType.EXTERNAL_TRAINING_DAYS.getAbsenceName(),
            AbsenceType.HOME_OFFICE_DAYS.getAbsenceName(),
            AbsenceType.COMPENSATORY_DAYS.getAbsenceName() // compensatory days must have an impact on the overtime balance
    );


    // Calculator functions for ProjectTime

    public String getInternalTimesForEmployee(@Nonnull List<ProjectTime> projektzeitTypeList, @Nonnull Employee employee) {
        Duration internalTimesForEmployee = getWorkingTimesForEmployee(projektzeitTypeList, employee, Predicate.not(ProjectTime::getBillable));
        return DurationFormatUtils.formatDuration(internalTimesForEmployee.toMillis(), BILLABLE_TIME_FORMAT);
    }

    public String getBillableTimesForEmployee(@Nonnull List<ProjectTime> projektzeitTypeList, @Nonnull Employee employee) {
        Duration billableTimesForEmployee = getWorkingTimesForEmployee(projektzeitTypeList, employee, ProjectTime::getBillable);
        return DurationFormatUtils.formatDuration(billableTimesForEmployee.toMillis(), BILLABLE_TIME_FORMAT);

    }

    public String getTotalWorkingTimeForEmployee(@Nonnull List<ProjectTime> projektzeitTypeList, @Nonnull Employee employee) {
        Duration totalWorkingTimeForEmployee = getWorkingTimesForEmployee(projektzeitTypeList, employee, $ -> true);
        return DurationFormatUtils.formatDuration(totalWorkingTimeForEmployee.toMillis(), BILLABLE_TIME_FORMAT);
    }

    public double getOvertimeForEmployee(Employee employee,
                                         List<ProjectTime> billableEntries,
                                         List<AbsenceTime> fehlzeitTypeList,
                                         LocalDate date) {
        if (employee.getRegularWorkingHours() == null) {
            return 0.0;
        }

        // In case there are absences that do not affect the current month, filter them out
        fehlzeitTypeList = fehlzeitTypeList.stream()
                .filter(ftl -> ftl.fromDate().getMonthValue() == date.getMonthValue())
                .toList();

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

    private Duration getWorkingTimesForEmployee(List<ProjectTime> projektzeitTypeList,
                                                Employee employee,
                                                Predicate<ProjectTime> billableFilter) {
        return projektzeitTypeList.stream()
                .filter(pzt -> pzt.getUserId().equals(employee.getUserId()))
                .filter(billableFilter)
                .map(pzt -> LocalTime.parse(pzt.getDuration()))
                .map(lt -> Duration.between(LocalTime.MIN, lt))
                .reduce(Duration.ZERO, Duration::plus);
    }

    // Calculator functions for AbsenceTime

    public int getAbsenceTimesForEmployee(@Nonnull List<AbsenceTime> fehlZeitTypeList, String absenceType, LocalDate date) {
        return (int) fehlZeitTypeList.stream()
                .filter(fzt -> fzt.reason().equals(absenceType))
                .filter(AbsenceTime::accepted)
                .map(fehlzeitType -> trimDurationToCurrentMonth(fehlzeitType, date))
                .mapToLong(ftl ->
                        getWorkingDaysBetween(
                                ftl.fromDate(),
                                ftl.toDate()
                        ).size()
                )
                .sum();
    }

    public Map<DayOfWeek, Long> getAbsenceDaysCountMap(@Nonnull List<AbsenceTime> fehlZeitTypeList, LocalDate date) {
        return fehlZeitTypeList.stream()
                .filter(ftl -> !BOOKABLE_ABSENCES.contains(ftl.reason()))
                .filter(AbsenceTime::accepted)
                .map(fehlzeitType -> trimDurationToCurrentMonth(fehlzeitType, date))
                .map(ftl -> getWorkingDaysBetween(ftl.fromDate(), ftl.toDate()))
                .flatMap(this::getDayOfWeeks)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private Stream<DayOfWeek> getDayOfWeeks(List<LocalDate> dates) {
        return dates.stream().map(LocalDate::getDayOfWeek);
    }

    private AbsenceTime trimDurationToCurrentMonth(AbsenceTime fehlzeit, LocalDate date) {
        LocalDate toDate = fehlzeit.toDate();
        if (toDate.getMonthValue() > date.getMonthValue()) {
            toDate = date.with(TemporalAdjusters.lastDayOfMonth());
        }
        LocalDate fromDate = fehlzeit.fromDate();
        if (fromDate.getMonthValue() < date.getMonthValue()) {
            fromDate = date.with(TemporalAdjusters.firstDayOfMonth());
        }

        return AbsenceTime.builder()
            .accepted(fehlzeit.accepted())
            .reason(fehlzeit.reason())
            .toDate(toDate)
            .fromDate(fromDate)
            .build();

    }
}
