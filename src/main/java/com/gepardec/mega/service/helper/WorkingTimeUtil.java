package com.gepardec.mega.service.helper;

import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil.getWorkingDaysBetween;
import static com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil.getWorkingDaysForYearMonth;

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

    public String getTotalWorkingTimeForEmployee(@Nonnull List<ProjectEntry> projektzeitTypeList) {
        Duration totalWorkingTimeForEmployee = getWorkingTimes(projektzeitTypeList);
        return DurationFormatUtils.formatDuration(totalWorkingTimeForEmployee.toMillis(), BILLABLE_TIME_FORMAT);
    }

    public double getOvertimeForEmployee(Employee employee,
                                         List<ProjectEntry> projectEntries,
                                         List<AbsenceTime> fehlzeitTypeList,
                                         YearMonth payrollMonth) {
        if (employee.getRegularWorkingTimes() == null) {
            return 0.0;
        }

        // In case there are absences that do not affect the current month, filter them out
        fehlzeitTypeList = fehlzeitTypeList.stream()
                .filter(ftl -> ftl.fromDate().getMonthValue() == payrollMonth.getMonthValue())
                .toList();

        var workingDaysCountMap = getWorkingDaysForYearMonth(payrollMonth)
                .stream()
                .collect(Collectors.groupingBy(LocalDate::getDayOfWeek, Collectors.counting()));
        var absenceDaysCountMap = getAbsenceDaysCountMap(fehlzeitTypeList, payrollMonth);
        var presentDaysCountMap = workingDaysCountMap.entrySet().stream()
                .map(entry -> removeAbsenceDays(entry, absenceDaysCountMap))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        var monthlyRegularWorkingHours = presentDaysCountMap.entrySet().stream()
                .map(entry ->
                        employee.getRegularWorkingTimes().active(payrollMonth)
                                .orElseThrow(() -> new IllegalStateException("Employee %s has no regular working times for the given payroll month".formatted(employee.getUserId())))
                                .workingHours()
                                .get(entry.getKey())
                                .multipliedBy(entry.getValue())
                )
                .reduce(Duration::plus)
                .orElse(Duration.ZERO);

        Duration totalWorkingHours = getWorkingTimes(projectEntries);
        Duration overtime = totalWorkingHours.minus(monthlyRegularWorkingHours);

        return (double) overtime.toMinutes() / 60;
    }

    public Duration getDurationFromTimeString(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            throw new IllegalArgumentException("Time string cannot be null or empty.");
        }
        if (!timeString.contains(":")) {
            throw new IllegalArgumentException("Invalid time string %s. Expected format is 'HH:MM'.".formatted(timeString));
        }
        String[] parts = timeString.split(":");
        return Duration.parse("PT%sH%sM".formatted(parts[0], parts[1]));
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

    private Duration getWorkingTimes(List<ProjectEntry> projectEntries) {
        return projectEntries.stream()
                .map(ProjectEntry::getDurationInHours)
                .map(hours -> Duration.ofMinutes(Double.valueOf(hours * 60).longValue()))
                .reduce(Duration.ZERO, Duration::plus);
    }

    // Calculator functions for AbsenceTime

    public int getAbsenceTimesForEmployee(@Nonnull List<AbsenceTime> fehlZeitTypeList, String absenceType, YearMonth payrollMonth) {
        return (int) fehlZeitTypeList.stream()
                .filter(fzt -> fzt.reason().equals(absenceType))
                .filter(AbsenceTime::accepted)
                .map(fehlzeitType -> trimDurationToCurrentMonth(fehlzeitType, payrollMonth))
                .mapToLong(ftl ->
                        getWorkingDaysBetween(
                                ftl.fromDate(),
                                ftl.toDate()
                        ).size()
                )
                .sum();
    }

    public Map<DayOfWeek, Long> getAbsenceDaysCountMap(@Nonnull List<AbsenceTime> fehlZeitTypeList, YearMonth payrollMonth) {
        return fehlZeitTypeList.stream()
                .filter(ftl -> !BOOKABLE_ABSENCES.contains(ftl.reason()))
                .filter(AbsenceTime::accepted)
                .map(fehlzeitType -> trimDurationToCurrentMonth(fehlzeitType, payrollMonth))
                .map(ftl -> getWorkingDaysBetween(ftl.fromDate(), ftl.toDate()))
                .flatMap(this::getDayOfWeeks)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private Stream<DayOfWeek> getDayOfWeeks(List<LocalDate> dates) {
        return dates.stream().map(LocalDate::getDayOfWeek);
    }

    private AbsenceTime trimDurationToCurrentMonth(AbsenceTime fehlzeit, YearMonth payrollMonth) {
        LocalDate toDate = fehlzeit.toDate();
        if (toDate.getMonthValue() > payrollMonth.getMonthValue()) {
            toDate = payrollMonth.atEndOfMonth();
        }
        LocalDate fromDate = fehlzeit.fromDate();
        if (fromDate.getMonthValue() < payrollMonth.getMonthValue()) {
            fromDate = payrollMonth.atDay(1);
        }

        return AbsenceTime.builder()
                .accepted(fehlzeit.accepted())
                .reason(fehlzeit.reason())
                .toDate(toDate)
                .fromDate(fromDate)
                .build();
    }
}
