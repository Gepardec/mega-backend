package com.gepardec.mega.domain.calculation.time;

import com.gepardec.mega.domain.calculation.AbstractTimeWarningCalculationStrategy;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.monthlyreport.AbsenteeType;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarningType;
import com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class NoEntryCalculator extends AbstractTimeWarningCalculationStrategy {

    public List<TimeWarning> calculate(@NotNull Employee employee, @NotNull List<ProjectEntry> projectEntries, @NotNull List<AbsenceTime> absenceEntries) {
        if (projectEntries.isEmpty()) {
            TimeWarning timeWarning = new TimeWarning();
            timeWarning.getWarningTypes().add(TimeWarningType.EMPTY_ENTRY_LIST);
            List<TimeWarning> timeWarnings = new ArrayList<>();
            timeWarnings.add(timeWarning);

            return timeWarnings;
        }

        List<LocalDate> futureDays = getFutureDays();
        List<LocalDate> regularWorking0Days = getRegularWorkingHours0Dates(
                employee,
                projectEntries.get(0)
                        .getDate()
                        .getYear(), projectEntries.get(0).getDate().getMonth().getValue()
        );
        List<LocalDate> compensatoryDays = filterAbsenceTypesAndCompileLocalDateList(AbsenteeType.COMPENSATORY_DAYS.getType(), absenceEntries);
        List<LocalDate> vacationDays = filterAbsenceTypesAndCompileLocalDateList(AbsenteeType.VACATION_DAYS.getType(), absenceEntries);
        List<LocalDate> sicknessDays = filterAbsenceTypesAndCompileLocalDateList(AbsenteeType.SICKNESS_DAYS.getType(), absenceEntries);
        List<LocalDate> nursingDays = filterAbsenceTypesAndCompileLocalDateList(AbsenteeType.NURSING_DAYS.getType(), absenceEntries);
        List<LocalDate> maternityLeaveDays = filterAbsenceTypesAndCompileLocalDateList(AbsenteeType.MATERNITY_LEAVE_DAYS.getType(), absenceEntries);
        List<LocalDate> externalTrainingDays = filterAbsenceTypesAndCompileLocalDateList(AbsenteeType.EXTERNAL_TRAINING_DAYS.getType(), absenceEntries);
        List<LocalDate> conferenceDays = filterAbsenceTypesAndCompileLocalDateList(AbsenteeType.CONFERENCE_DAYS.getType(), absenceEntries);
        List<LocalDate> maternityProtectionDays = filterAbsenceTypesAndCompileLocalDateList(AbsenteeType.MATERNITY_PROTECTION_DAYS.getType(), absenceEntries);
        List<LocalDate> fatherMonthDays = filterAbsenceTypesAndCompileLocalDateList(AbsenteeType.FATHER_MONTH_DAYS.getType(), absenceEntries);
        List<LocalDate> paidSpecialLeaveDays = filterAbsenceTypesAndCompileLocalDateList(AbsenteeType.PAID_SPECIAL_LEAVE_DAYS.getType(), absenceEntries);
        List<LocalDate> nonPaidVacationDays = filterAbsenceTypesAndCompileLocalDateList(AbsenteeType.NON_PAID_VACATION_DAYS.getType(), absenceEntries);
        List<LocalDate> bookedDays = projectEntries.stream()
                .map(ProjectEntry::getDate)
                .toList();

        LocalDate firstWorkingDay = employee.getFirstDayCurrentEmploymentPeriod();

        YearMonth yearMonth = YearMonth.of(projectEntries.get(0).getDate().getYear(), projectEntries.get(0).getDate().getMonth().getValue());
        return OfficeCalendarUtil.getWorkingDaysForYearMonth(yearMonth).stream()
                .filter(date -> !firstWorkingDay.isAfter(date))
                .filter(date -> !compensatoryDays.contains(date))
                .filter(date -> !vacationDays.contains(date))
                .filter(date -> !sicknessDays.contains(date))
                .filter(date -> !bookedDays.contains(date))
                .filter(date -> !nursingDays.contains(date))
                .filter(date -> !maternityLeaveDays.contains(date))
                .filter(date -> !externalTrainingDays.contains(date))
                .filter(date -> !conferenceDays.contains(date))
                .filter(date -> !maternityProtectionDays.contains(date))
                .filter(date -> !fatherMonthDays.contains(date))
                .filter(date -> !paidSpecialLeaveDays.contains(date))
                .filter(date -> !nonPaidVacationDays.contains(date))
                .filter(date -> !regularWorking0Days.contains(date))
                .filter(date -> !futureDays.contains(date))
                .map(this::createTimeWarning)
                .distinct()
                .toList();
    }

    private List<LocalDate> getRegularWorkingHours0Dates(Employee employee, int year, int month) {
        List<LocalDate> allNonRegularWorkingHourDates = new ArrayList<>();

        if (employee.getRegularWorkingHours() == null) {
            return allNonRegularWorkingHourDates;
        }

        employee.getRegularWorkingHours().forEach((dayOfWeek, regularHours) -> {
            if (regularHours.isZero()) {
                LocalDate upCountingDay = LocalDate.of(year, month, 1).with(TemporalAdjusters.firstInMonth(dayOfWeek));

                allNonRegularWorkingHourDates.add(upCountingDay);
                while ((upCountingDay = upCountingDay.with(TemporalAdjusters.next(dayOfWeek))).getMonthValue() == month) {
                    allNonRegularWorkingHourDates.add(upCountingDay);
                }
            }
        });
        return allNonRegularWorkingHourDates;
    }

    private List<LocalDate> getFutureDays() {
        LocalDate today = LocalDate.now();
        return today.datesUntil(today.with(TemporalAdjusters.firstDayOfNextMonth()))
                .toList();
    }

    private TimeWarning createTimeWarning(final LocalDate date) {
        TimeWarning timeWarning = new TimeWarning();
        timeWarning.setDate(date);
        timeWarning.getWarningTypes().add(TimeWarningType.NO_TIME_ENTRY);

        return timeWarning;
    }

    private List<LocalDate> filterAbsenceTypesAndCompileLocalDateList(String type, List<AbsenceTime> absenceEntries) {
        return absenceEntries.stream()
                .filter(fzt -> fzt.reason().equals(type))
                .flatMap(this::extractFehlzeitenDateRange)
                .toList();
    }

    private Stream<LocalDate> extractFehlzeitenDateRange(AbsenceTime fzt) {
        return fzt.fromDate().datesUntil(fzt.toDate().plusDays(1));
    }
}
