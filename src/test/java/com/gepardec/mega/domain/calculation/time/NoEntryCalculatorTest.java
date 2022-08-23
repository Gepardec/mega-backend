package com.gepardec.mega.domain.calculation.time;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.monthlyreport.AbsenteeType;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarningType;
import de.provantis.zep.FehlzeitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
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
                .containsExactly(expectedTimeWarningsList.get(0).getDate());
    }

    @Test
    void calculate_whenAllEntries_thenNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryList(0), new ArrayList<>());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenAllEntriesAndVacationDayOnWorkingDay_thenNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryList(2), createAbsenceListFromUBType());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenAllEntriesAndCompensatoryDayOnWorkingDay_thenNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryList(2), createAbsenceListFromFAType());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenAllEntriesAndSicknessDayOnWorkingDay_thenNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryList(2), createAbsenceListFromKRType());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenAllEntriesInMonthWithHolidayOnFirstNov_thenNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), new ArrayList<>());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenNursingDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromPUType());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenMaternityLeaveDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromKAType());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenExternalTrainingDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromEWType());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenConferenceDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromKOType());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenMaternityProtectionDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromMUType());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenFatherMonthDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromPAType());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenPaidSpecialLeaveDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromSUType());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenNonPaidVacationDays_thenReturnsNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), createProjectEntryListForNovember(), createAbsenceListFromUUType());

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenDateInFuture_thenNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), List.of(createProjectTimeEntryForFuture(LocalDate.now())), createAbsenceListFromUUType());
        List<TimeWarning> resultsAfterToday = result.stream()
                .filter(timeWarning -> timeWarning.getDate().isAfter(LocalDate.now()))
                .collect(Collectors.toList());


        assertThat(resultsAfterToday).isEmpty();
    }

    @Test
    void calculate_whenDateToday_thenNoWarning() {
        List<TimeWarning> result = noEntryCalculator.calculate(createEmployee(), List.of(createProjectTimeEntryForFuture(LocalDate.now())), createAbsenceListFromUUType());
        List<TimeWarning> resultsAfterToday = result.stream()
                .filter(timeWarning -> timeWarning.getDate().isEqual(LocalDate.now()))
                .collect(Collectors.toList());


        assertThat(resultsAfterToday).isEmpty();
    }

    private List<FehlzeitType> createAbsenceListFromUBType() {
        List<FehlzeitType> fehlzeiten = new ArrayList<>();
        FehlzeitType fehlzeitType = new FehlzeitType();
        String startDate = LocalDate.of(2021, 2, 25).toString();
        String endDate = LocalDate.of(2021, 2, 26).toString();
        fehlzeitType.setStartdatum(startDate);
        fehlzeitType.setEnddatum(endDate);
        fehlzeitType.setFehlgrund(AbsenteeType.VACATION_DAYS.getType());
        fehlzeiten.add(fehlzeitType);

        return fehlzeiten;
    }

    private List<FehlzeitType> createAbsenceListFromFAType() {
        List<FehlzeitType> fehlzeiten = new ArrayList<>();
        FehlzeitType fehlzeitType = new FehlzeitType();
        String startDate = LocalDate.of(2021, 2, 25).toString();
        String endDate = LocalDate.of(2021, 2, 26).toString();
        fehlzeitType.setStartdatum(startDate);
        fehlzeitType.setEnddatum(endDate);
        fehlzeitType.setFehlgrund(AbsenteeType.COMPENSATORY_DAYS.getType());
        fehlzeiten.add(fehlzeitType);

        return fehlzeiten;
    }

    private List<FehlzeitType> createAbsenceListFromKRType() {
        List<FehlzeitType> fehlzeiten = new ArrayList<>();
        FehlzeitType fehlzeitType = new FehlzeitType();
        String startDate = LocalDate.of(2021, 2, 25).toString();
        String endDate = LocalDate.of(2021, 2, 26).toString();
        fehlzeitType.setStartdatum(startDate);
        fehlzeitType.setEnddatum(endDate);
        fehlzeitType.setFehlgrund(AbsenteeType.SICKNESS_DAYS.getType());
        fehlzeiten.add(fehlzeitType);

        return fehlzeiten;
    }

    private List<FehlzeitType> createAbsenceListFromPUType() {
        List<FehlzeitType> fehlzeiten = new ArrayList<>();
        FehlzeitType fehlzeitType = new FehlzeitType();
        String startDate = LocalDate.of(2021, 2, 25).toString();
        String endDate = LocalDate.of(2021, 2, 26).toString();
        fehlzeitType.setStartdatum(startDate);
        fehlzeitType.setEnddatum(endDate);
        fehlzeitType.setFehlgrund(AbsenteeType.NURSING_DAYS.getType());
        fehlzeiten.add(fehlzeitType);

        return fehlzeiten;
    }

    private List<FehlzeitType> createAbsenceListFromKAType() {
        List<FehlzeitType> fehlzeiten = new ArrayList<>();
        FehlzeitType fehlzeitType = new FehlzeitType();
        String startDate = LocalDate.of(2021, 2, 25).toString();
        String endDate = LocalDate.of(2021, 2, 26).toString();
        fehlzeitType.setStartdatum(startDate);
        fehlzeitType.setEnddatum(endDate);
        fehlzeitType.setFehlgrund(AbsenteeType.MATERNITY_LEAVE_DAYS.getType());
        fehlzeiten.add(fehlzeitType);

        return fehlzeiten;
    }

    private List<FehlzeitType> createAbsenceListFromEWType() {
        List<FehlzeitType> fehlzeiten = new ArrayList<>();
        FehlzeitType fehlzeitType = new FehlzeitType();
        String startDate = LocalDate.of(2021, 2, 25).toString();
        String endDate = LocalDate.of(2021, 2, 26).toString();
        fehlzeitType.setStartdatum(startDate);
        fehlzeitType.setEnddatum(endDate);
        fehlzeitType.setFehlgrund(AbsenteeType.EXTERNAL_TRAINING_DAYS.getType());
        fehlzeiten.add(fehlzeitType);

        return fehlzeiten;
    }

    private List<FehlzeitType> createAbsenceListFromKOType() {
        List<FehlzeitType> fehlzeiten = new ArrayList<>();
        FehlzeitType fehlzeitType = new FehlzeitType();
        String startDate = LocalDate.of(2021, 2, 25).toString();
        String endDate = LocalDate.of(2021, 2, 26).toString();
        fehlzeitType.setStartdatum(startDate);
        fehlzeitType.setEnddatum(endDate);
        fehlzeitType.setFehlgrund(AbsenteeType.CONFERENCE_DAYS.getType());
        fehlzeiten.add(fehlzeitType);

        return fehlzeiten;
    }

    private List<FehlzeitType> createAbsenceListFromMUType() {
        List<FehlzeitType> fehlzeiten = new ArrayList<>();
        FehlzeitType fehlzeitType = new FehlzeitType();
        String startDate = LocalDate.of(2021, 2, 25).toString();
        String endDate = LocalDate.of(2021, 2, 26).toString();
        fehlzeitType.setStartdatum(startDate);
        fehlzeitType.setEnddatum(endDate);
        fehlzeitType.setFehlgrund(AbsenteeType.MATERNITY_PROTECTION_DAYS.getType());
        fehlzeiten.add(fehlzeitType);

        return fehlzeiten;
    }

    private List<FehlzeitType> createAbsenceListFromPAType() {
        List<FehlzeitType> fehlzeiten = new ArrayList<>();
        FehlzeitType fehlzeitType = new FehlzeitType();
        String startDate = LocalDate.of(2021, 2, 25).toString();
        String endDate = LocalDate.of(2021, 2, 26).toString();
        fehlzeitType.setStartdatum(startDate);
        fehlzeitType.setEnddatum(endDate);
        fehlzeitType.setFehlgrund(AbsenteeType.FATHER_MONTH_DAYS.getType());
        fehlzeiten.add(fehlzeitType);

        return fehlzeiten;
    }

    private List<FehlzeitType> createAbsenceListFromSUType() {
        List<FehlzeitType> fehlzeiten = new ArrayList<>();
        FehlzeitType fehlzeitType = new FehlzeitType();
        String startDate = LocalDate.of(2021, 2, 25).toString();
        String endDate = LocalDate.of(2021, 2, 26).toString();
        fehlzeitType.setStartdatum(startDate);
        fehlzeitType.setEnddatum(endDate);
        fehlzeitType.setFehlgrund(AbsenteeType.PAID_SPECIAL_LEAVE_DAYS.getType());
        fehlzeiten.add(fehlzeitType);

        return fehlzeiten;
    }

    private List<FehlzeitType> createAbsenceListFromUUType() {
        List<FehlzeitType> fehlzeiten = new ArrayList<>();
        FehlzeitType fehlzeitType = new FehlzeitType();
        String startDate = LocalDate.of(2021, 2, 25).toString();
        String endDate = LocalDate.of(2021, 2, 26).toString();
        fehlzeitType.setStartdatum(startDate);
        fehlzeitType.setEnddatum(endDate);
        fehlzeitType.setFehlgrund(AbsenteeType.NON_PAID_VACATION_DAYS.getType());
        fehlzeiten.add(fehlzeitType);

        return fehlzeiten;
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

        final Employee employee = Employee.builder()
                .email(name + "@gepardec.com")
                .firstname(name)
                .lastname(name + "_Nachname")
                .title("Ing.")
                .userId(String.valueOf(userId))
                .salutation("Herr")
                .workDescription("ARCHITEKT")
                .releaseDate(releaseDate)
                .active(true)
                .build();

        return employee;
    }
}

