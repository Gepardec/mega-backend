package com.gepardec.mega.service.impl.monthlyreport;


import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.monthlyreport.JourneyTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.Task;
import com.gepardec.mega.domain.model.monthlyreport.WorkingLocation;
import com.gepardec.mega.service.helper.WorkingTimeUtil;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@QuarkusTest
class WorkingTimeUtilTest {

    @Inject
    WorkingTimeUtil workingTimeUtil;


    static Stream<String> invalidTimeStrings() {
        return Stream.of(
                "01.30",
                "2.00",
                "",
                null
        );
    }

    @Test
    void getInternalTimesForEmployeeTest() {
        Employee employee = createEmployee().build();

        List<ProjectTime> projectTimes = returnNormalDayProjectTimes(5);
        String internalTimesForEmployee = workingTimeUtil.getInternalTimesForEmployee(projectTimes, employee);
        assertThat(internalTimesForEmployee).isEqualTo("20:00");
    }

    @Test
    void getBillableTimesForEmployeeTest() {
        Employee employee = createEmployee().build();

        List<ProjectTime> projectTimes = returnNormalDayProjectTimes(5);
        String internalTimesForEmployee = workingTimeUtil.getBillableTimesForEmployee(projectTimes, employee);
        assertThat(internalTimesForEmployee).isEqualTo("20:00");
    }

    @Test
    void getTotalWorkingTimes_ProjectTime() {
        Employee employee = createEmployee().build();
        List<ProjectEntry> projectEntries = getProjectentries();
        String totalWorkingTimes = workingTimeUtil.getTotalWorkingTimeForEmployee(projectEntries, employee);
        assertThat(totalWorkingTimes).isEqualTo("24:15");
    }

    @Test
    void getOvertimeForEmployee_RETURN_POSITIVE_OVERTIME() {
        Employee employee = createEmployee().build();

        List<ProjectEntry> projectTimes = returnNormalDayProjectEntries(5);
        List<AbsenceTime> fehlzeitTypes = List.of();

        double overtimeforEmployee = workingTimeUtil.getOvertimeForEmployee(employee, projectTimes, fehlzeitTypes, YearMonth.of(2023, 11));
        assertThat(overtimeforEmployee).isEqualTo(8.0);
    }

    @Test
    void getOvertimeForEmployee_RETURN_NEGATIVE_OVERTIME() {
        Employee employee = createEmployee().build();

        List<ProjectEntry> projectTimes = returnNormalDayProjectEntries(3);
        List<AbsenceTime> fehlzeitTypes = List.of();

        double overtimeforEmployee = workingTimeUtil.getOvertimeForEmployee(employee, projectTimes, fehlzeitTypes, YearMonth.of(2023, 11));
        assertThat(overtimeforEmployee).isEqualTo(-8.);
    }

    @Test
    void getOvertimeForEmployee_WITH_ABSENCE() {
        Employee employee = createEmployee().build();

        List<ProjectEntry> projectTimes = returnNormalDayProjectEntries(3);
        List<AbsenceTime> fehlzeitTypes = returnFehlzeitTypeList();

        double overtimeforEmployee = workingTimeUtil.getOvertimeForEmployee(employee, projectTimes, fehlzeitTypes, YearMonth.of(2023, 11));
        assertThat(overtimeforEmployee).isZero();
    }

    @Test
    void getOvertimeForEmployee_WITH_HOLIDAY() {
        Map<DayOfWeek, Duration> regularWorkingHours = Map.ofEntries(
                Map.entry(DayOfWeek.MONDAY, Duration.ofHours(0)),
                Map.entry(DayOfWeek.TUESDAY, Duration.ofHours(0)),
                Map.entry(DayOfWeek.WEDNESDAY, Duration.ofHours(0)),
                Map.entry(DayOfWeek.THURSDAY, Duration.ofHours(8)),
                Map.entry(DayOfWeek.FRIDAY, Duration.ofHours(0)),
                Map.entry(DayOfWeek.SATURDAY, Duration.ofHours(0)),
                Map.entry(DayOfWeek.SUNDAY, Duration.ofHours(0)));

        Range<LocalDate> range = Range.of(LocalDate.MIN, LocalDate.now());

        Map<Range<LocalDate>,Map<DayOfWeek, Duration>> regularWorkingHoursWithRange =
                Map.of(range, regularWorkingHours);

        Employee employee = createEmployee().regularWorkingHours(regularWorkingHoursWithRange).build();

        List<ProjectEntry> projectTimes = returnNormalDayProjectEntries(3);
        List<AbsenceTime> fehlzeitTypes = returnFehlzeitTypeList();

        double overtimeforEmployee = workingTimeUtil.getOvertimeForEmployee(
                employee,
                projectTimes,
                fehlzeitTypes,
                YearMonth.of(2023, 10)
        );
        assertThat(overtimeforEmployee).isEqualTo(0);
    }

    @Test
    void getAbsenceTimesForEmployee() {
        Employee employee = createEmployee().build();

        List<AbsenceTime> fehlzeitTypes = returnFehlzeitTypeList();
        int absenceTimesForEmployee = workingTimeUtil.getAbsenceTimesForEmployee(fehlzeitTypes, "UB", YearMonth.of(2023, 11));
        assertThat(absenceTimesForEmployee).isEqualTo(2);
    }

    @ParameterizedTest
    @CsvSource({
            "01:30, PT1H30M",
            "00:45, PT0H45M",
            "12:00, PT12H0M",
            "23:59, PT23H59M",
            "00:00, PT0H0M"
    })
    void getDurationFromTimeString_whenInputStringIsValid_ReturnsDuration(String input, String expected) {
        Duration expectedDuration = Duration.parse(expected);
        Duration actualDuration = workingTimeUtil.getDurationFromTimeString(input);
        assertThat(expectedDuration).isEqualTo(actualDuration);
    }


    @ParameterizedTest
    @MethodSource("invalidTimeStrings")
    void getDurationFromTimeString_whenInputStringIsInvalid_ThrowsIllegalArgumentException(String input) {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> workingTimeUtil.getDurationFromTimeString(input));
    }


    private List<AbsenceTime> returnFehlzeitTypeList() {
        AbsenceTime fehlzeitType = AbsenceTime.builder()
                .fromDate(LocalDate.of(2023, 11, 6))
                .toDate(LocalDate.of(2023, 11, 7))
                .reason("UB")
                .accepted(true)
                .build();

        return List.of(fehlzeitType);
    }

    private List<ProjectTime> returnNormalDayProjectTimes(int times) {
        ProjectTime projektzeitType = ProjectTime.builder().build();
        projektzeitType.setDuration("04:00");
        projektzeitType.setUserId("1");
        projektzeitType.setBillable(false);

        ProjectTime projektzeitTypeBilllable = ProjectTime.builder().build();
        projektzeitTypeBilllable.setDuration("04:00");
        projektzeitTypeBilllable.setUserId("1");
        projektzeitTypeBilllable.setBillable(true);


        List<ProjectTime> projectTimes = new ArrayList<>();

        for (int i = 0; i < times; i++) {
            projectTimes.add(projektzeitTypeBilllable);
            projectTimes.add(projektzeitType);
        }
        return projectTimes;
    }

    private Employee.Builder createEmployee() {
        User user = User.builder()
                .dbId(1)
                .userId("1")
                .email("max.mustermann@gpeardec.com")
                .firstname("Max")
                .lastname("Mustermann")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        Map<DayOfWeek, Duration> regularWorkingHours = Map.ofEntries(
                Map.entry(DayOfWeek.MONDAY, Duration.ofHours(8)),
                Map.entry(DayOfWeek.TUESDAY, Duration.ofHours(0)),
                Map.entry(DayOfWeek.WEDNESDAY, Duration.ofHours(0)),
                Map.entry(DayOfWeek.THURSDAY, Duration.ofHours(0)),
                Map.entry(DayOfWeek.FRIDAY, Duration.ofHours(0)),
                Map.entry(DayOfWeek.SATURDAY, Duration.ofHours(0)),
                Map.entry(DayOfWeek.SUNDAY, Duration.ofHours(0)));

        Range<LocalDate> range = Range.of(LocalDate.MIN, LocalDate.now());

        Map<Range<LocalDate>,Map<DayOfWeek, Duration>> regularWorkingHoursWithRange =
                Map.of(range, regularWorkingHours);


        return Employee.builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .title("Ing.")
                .userId(user.getUserId())
                .releaseDate("2020-01-01")
                .active(true)
                .regularWorkingHours(regularWorkingHoursWithRange);
    }

    private List<ProjectEntry> returnNormalDayProjectEntries(int times){
        List<ProjectEntry> projectTimes = new ArrayList<>();
        for (int i = 1; i <= times; i++) {

            ProjectEntry projektzeitType = ProjectTimeEntry.builder()
                .fromTime(LocalDateTime.of(2023, 11, times, 8, 0))
                .toTime(LocalDateTime.of(2023, 11, times, 12, 0))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
                .process("1")
                .build();
        ProjectEntry projektzeitTypeBilllable = ProjectTimeEntry.builder()
                .fromTime(LocalDateTime.of(2023, 11, times, 13, 0))
                .toTime(LocalDateTime.of(2023, 11, times, 17, 0))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
                .process("1")
                .build();

            projectTimes.add(projektzeitTypeBilllable);
            projectTimes.add(projektzeitType);
        }
        return projectTimes;
    }

    private List<ProjectEntry> getProjectentries() {
        return List.of(
                ProjectTimeEntry.builder()
                        .fromTime(LocalDateTime.of(2023, 11, 1, 8, 0))
                        .toTime(LocalDateTime.of(2023, 11, 1, 12, 15))
                        .task(Task.BEARBEITEN)
                        .workingLocation(WorkingLocation.MAIN)
                        .process("1")
                        .build(),
                ProjectTimeEntry.builder()
                        .fromTime(LocalDateTime.of(2023, 11, 1, 13, 0))
                        .toTime(LocalDateTime.of(2023, 11, 1, 17, 0))
                        .task(Task.BEARBEITEN)
                        .workingLocation(WorkingLocation.MAIN)
                        .process("1")
                        .build(),
                ProjectTimeEntry.builder()
                        .fromTime(LocalDateTime.of(2023, 11, 2, 8, 0))
                        .toTime(LocalDateTime.of(2023, 11, 2, 12, 0))
                        .task(Task.BEARBEITEN)
                        .workingLocation(WorkingLocation.MAIN)
                        .process("1")
                        .build(),
                ProjectTimeEntry.builder()
                        .fromTime(LocalDateTime.of(2023, 11, 2, 13, 0))
                        .toTime(LocalDateTime.of(2023, 11, 2, 17, 0))
                        .task(Task.BEARBEITEN)
                        .workingLocation(WorkingLocation.MAIN)
                        .process("1")
                        .build(),
                JourneyTimeEntry.builder()
                        .fromTime(LocalDateTime.of(2023, 11, 3, 8, 0))
                        .toTime(LocalDateTime.of(2023, 11, 3, 12, 0))
                        .task(Task.REISEN)
                        .workingLocation(WorkingLocation.MAIN)
                        .build(),
                JourneyTimeEntry.builder()
                        .fromTime(LocalDateTime.of(2023, 11, 3, 13, 0))
                        .toTime(LocalDateTime.of(2023, 11, 3, 17, 0))
                        .task(Task.REISEN)
                        .workingLocation(WorkingLocation.MAIN)
                        .build()
        );
    }
}
