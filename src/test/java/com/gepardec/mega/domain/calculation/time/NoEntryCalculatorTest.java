package com.gepardec.mega.domain.calculation.time;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.EmploymentPeriod;
import com.gepardec.mega.domain.model.EmploymentPeriods;
import com.gepardec.mega.domain.model.RegularWorkingTime;
import com.gepardec.mega.domain.model.RegularWorkingTimes;
import com.gepardec.mega.domain.model.monthlyreport.AbsenteeType;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarningType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class NoEntryCalculatorTest {

    private NoEntryCalculator noEntryCalculator;

    @BeforeEach
    void init() {
        noEntryCalculator = new NoEntryCalculator();
    }

    @Test
    void calculate_whenNoProjectEntriesAndNoAbsenceEntries_thenEmptyEntryListWarning() {
        TimeWarning expectedTimeWarning = new TimeWarning();
        expectedTimeWarning.getWarningTypes().add(TimeWarningType.EMPTY_ENTRY_LIST);
        List<TimeWarning> expectedTimeWarningsList = new ArrayList<>();
        expectedTimeWarningsList.add(expectedTimeWarning);

        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), new ArrayList<>(), new ArrayList<>());

        assertThat(result).hasSize(1).containsExactlyElementsOf(expectedTimeWarningsList);
    }

    @Test
    void calculate_whenMissingEntry_thenCorrectWarningWithCorrectDate() {
        TimeWarning expectedTimeWarning = new TimeWarning();
        expectedTimeWarning.getWarningTypes().add(TimeWarningType.EMPTY_ENTRY_LIST);
        expectedTimeWarning.setDate(LocalDate.of(2021, 2, 26));
        List<TimeWarning> expectedTimeWarningsList = new ArrayList<>();
        expectedTimeWarningsList.add(expectedTimeWarning);

        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryList(1), new ArrayList<>());

        assertThat(result).hasSize(1)
                .extracting(TimeWarning::getDate)
                .containsExactly(expectedTimeWarningsList.getFirst().getDate());
    }

    @Test
    void calculate_whenAllEntries_thenNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryList(0), new ArrayList<>());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenAllEntriesAndVacationDayOnWorkingDay_thenNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryList(2), createAbsenceListFromType(AbsenteeType.VACATION_DAYS));

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenAllEntriesAndCompensatoryDayOnWorkingDay_thenNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryList(2), createAbsenceListFromType(AbsenteeType.COMPENSATORY_DAYS));

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenAllEntriesAndSicknessDayOnWorkingDay_thenNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryList(2), createAbsenceListFromType(AbsenteeType.SICKNESS_DAYS));

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenAllEntriesInMonthWithHolidayOnFirstNov_thenNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), new ArrayList<>());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenNursingDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromType(AbsenteeType.NURSING_DAYS));

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenMaternityLeaveDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromType(AbsenteeType.MATERNITY_LEAVE_DAYS));

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenExternalTrainingDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromType(AbsenteeType.EXTERNAL_TRAINING_DAYS));

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenConferenceDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromType(AbsenteeType.CONFERENCE_DAYS));

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenMaternityProtectionDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromType(AbsenteeType.MATERNITY_PROTECTION_DAYS));

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenFatherMonthDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromType(AbsenteeType.FATHER_MONTH_DAYS));

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenPaidSpecialLeaveDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromType(AbsenteeType.PAID_SPECIAL_LEAVE_DAYS));

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenNonPaidVacationDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromType(AbsenteeType.NON_PAID_VACATION_DAYS));

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenDateInFuture_thenNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), List.of(createProjectTimeEntryForFuture(LocalDate.now())), createAbsenceListFromType(AbsenteeType.NON_PAID_VACATION_DAYS));
        List<TimeWarning> resultsAfterToday = result.stream()
                .filter(timeWarning -> timeWarning.getDate().isAfter(LocalDate.now()))
                .toList();


        assertThat(resultsAfterToday).isEmpty();
    }

    @Test
    void calculate_whenDateToday_thenNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), List.of(createProjectTimeEntryForFuture(LocalDate.now())), createAbsenceListFromType(AbsenteeType.NON_PAID_VACATION_DAYS));
        List<TimeWarning> resultsAfterToday = result.stream()
                .filter(timeWarning -> timeWarning.getDate().isEqual(LocalDate.now()))
                .toList();


        assertThat(resultsAfterToday).isEmpty();
    }

    @Test
    void calculate_whenDateBeforeFirstWorkingDay_thenNoWarning() {
        var projectEntryList = createProjectEntryList(0);
        projectEntryList.removeFirst();

        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), projectEntryList, Collections.emptyList());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenDateAfterFirstWorkingDay_thenWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), Collections.emptyList(), Collections.emptyList());


        assertThat(result).isNotEmpty();
    }

    private List<AbsenceTime> createAbsenceListFromType(AbsenteeType type) {
        LocalDate startDate = LocalDate.of(2021, 2, 25);
        LocalDate endDate = LocalDate.of(2021, 2, 26);
        String reason = type.getType();

        AbsenceTime absence = AbsenceTime.builder()
                .fromDate(startDate)
                .toDate(endDate)
                .reason(reason)
                .build();

        return new ArrayList<>(List.of(absence));
    }


    private List<ProjectEntry> createProjectEntryList(int amountOfMissingEntries) {
        return IntStream.rangeClosed(1, 26 - amountOfMissingEntries)
                .mapToObj(i -> createProjectTimeEntry(2, i))
                .collect(Collectors.toList());
    }

    private ProjectTimeEntry createProjectTimeEntryForFuture(LocalDate date) {
        return ProjectTimeEntry.builder()
                .fromTime(LocalDateTime.of(date.with(TemporalAdjusters.lastDayOfMonth()), LocalTime.of(12, 0)))
                .toTime(LocalDateTime.of(date.with(TemporalAdjusters.lastDayOfMonth()), LocalTime.of(8, 0)))
                .build();
    }

    private List<ProjectEntry> createProjectEntryListForNovember() {
        return IntStream.rangeClosed(2, 30)
                .mapToObj(i -> createProjectTimeEntry(11, i))
                .collect(Collectors.toList());
    }

    private ProjectTimeEntry createProjectTimeEntry(int month, int day) {
        return ProjectTimeEntry.builder()
                .fromTime(LocalDateTime.of(LocalDate.of(2021, month, day), LocalTime.of(8, 0)))
                .toTime(LocalDateTime.of(LocalDate.of(2021, month, day), LocalTime.of(12, 0)))
                .build();
    }

    private Employee createEmployee() {
        return createEmployeeWithReleaseDate(0, "2022-01-01");
    }

    private Employee createEmployeeWithReleaseDate(final int userId, String releaseDate) {
        final String name = "Max_" + userId;

        Map<DayOfWeek, Duration> regularWorkingHours = Map.ofEntries(
                Map.entry(DayOfWeek.MONDAY, Duration.ofHours(8)),
                Map.entry(DayOfWeek.TUESDAY, Duration.ofHours(8)),
                Map.entry(DayOfWeek.WEDNESDAY, Duration.ofHours(8)),
                Map.entry(DayOfWeek.THURSDAY, Duration.ofHours(8)),
                Map.entry(DayOfWeek.FRIDAY, Duration.ofHours(6)),
                Map.entry(DayOfWeek.SATURDAY, Duration.ofHours(0)),
                Map.entry(DayOfWeek.SUNDAY, Duration.ofHours(0)));

        final Employee employee = Employee.builder()
                .email(name + "@gepardec.com")
                .firstname(name)
                .lastname(name + "_Nachname")
                .title("Ing.")
                .userId(String.valueOf(userId))
                .salutation("Herr")
                .workDescription("ARCHITEKT")
                .releaseDate(releaseDate)
                .employmentPeriods(new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2021, 2, 2), null)))
                .regularWorkingTimes(new RegularWorkingTimes(List.of(new RegularWorkingTime(null, regularWorkingHours))))
                .build();

        return employee;
    }
}

