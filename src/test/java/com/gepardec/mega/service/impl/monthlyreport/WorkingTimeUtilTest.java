package com.gepardec.mega.service.impl.monthlyreport;


import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.RegularWorkingTime;
import com.gepardec.mega.domain.model.RegularWorkingTimes;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.monthlyreport.JourneyTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.Task;
import com.gepardec.mega.domain.model.monthlyreport.WorkingLocation;
import com.gepardec.mega.service.helper.WorkingTimeUtil;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@QuarkusTest
class WorkingTimeUtilTest {

    @Alternative
    @Priority(1)
    @ApplicationScoped
    public static class TestClockProducer {

        @Produces
        @ApplicationScoped
        public Clock clock() {
            return Clock.fixed(
                    LocalDate.of(2023, 11, 3).atStartOfDay(ZoneOffset.UTC).toInstant(),
                    ZoneOffset.UTC
            );
        }
    }

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

        List<ProjectTime> projectTimes = returnNormalDayProjectTimes();
        String internalTimesForEmployee = workingTimeUtil.getInternalTimesForEmployee(projectTimes, employee);
        assertThat(internalTimesForEmployee).isEqualTo("20:00");
    }

    @Test
    void getBillableTimesForEmployeeTest() {
        Employee employee = createEmployee().build();

        List<ProjectTime> projectTimes = returnNormalDayProjectTimes();
        String internalTimesForEmployee = workingTimeUtil.getBillableTimesForEmployee(projectTimes, employee);
        assertThat(internalTimesForEmployee).isEqualTo("20:00");
    }

    @Test
    void getTotalWorkingTimes_ProjectTime() {
        List<ProjectEntry> projectEntries = getProjectentries();
        String totalWorkingTimes = workingTimeUtil.getTotalWorkingTimeForEmployee(projectEntries);
        assertThat(totalWorkingTimes).isEqualTo("24:15");
    }

    @Nested
    class GetOvertimeForEmployee {

        @Nested
        class When1stOfNovemberIsHolidayAndTodayIs3rdOfNovember {

            @Test
            void whenWorkingTimeIsMoreThanRegular_thenShouldReturnPositiveOvertime() {
                Employee employee = createEmployee().build();
                List<ProjectEntry> projectEntries = new ArrayList<>();

                // 1 hour overtime per day for days 1-5, but since November 1st is a holiday and the clock is fixed to November 3rd,
                // only entries for November 2nd and 3rd are counted, resulting in a total of 2 hours overtime.
                for (int day = 1; day <= 5; day++) {
                    projectEntries.add(createProjectTimeEntry(day, LocalTime.of(8, 0), LocalTime.of(12, 0)));
                    projectEntries.add(createProjectTimeEntry(day, LocalTime.of(13, 0), LocalTime.of(18, 0)));
                }

                double overtime = workingTimeUtil.getOvertimeForEmployee(employee, projectEntries, List.of(), YearMonth.of(2023, 11));

                assertThat(overtime).isEqualTo(2d);
            }

            @Test
            void whenWorkingTimeIsLessThanRegular_thenShouldReturnNegativeOvertime() {
                Employee employee = createEmployee().build();
                List<ProjectEntry> projectEntries = new ArrayList<>();

                // Entries for 5 days, but only 2 working days (Nov 2nd and 3rd) are counted due to the fixed clock at Nov 3rd and Nov 1st being a holiday.
                // Each counted day is 1 hour less than regular, so total overtime = -2h.
                for (int day = 1; day <= 5; day++) {
                    projectEntries.add(createProjectTimeEntry(day, LocalTime.of(8, 0), LocalTime.of(12, 0)));
                    projectEntries.add(createProjectTimeEntry(day, LocalTime.of(13, 0), LocalTime.of(16, 0)));
                }

                double overtime = workingTimeUtil.getOvertimeForEmployee(employee, projectEntries, List.of(), YearMonth.of(2023, 11));

                assertThat(overtime).isEqualTo(-2d);
            }

            @Test
            void whenWorkingTimeIsRegular_thenShouldReturnZero() {
                Employee employee = createEmployee().build();
                List<ProjectEntry> projectEntries = new ArrayList<>();

                // Each day, working time matches the regular working hours (8 hours per day)
                for (int day = 1; day <= 5; day++) {
                    projectEntries.add(createProjectTimeEntry(day, LocalTime.of(8, 0), LocalTime.of(12, 0)));
                    projectEntries.add(createProjectTimeEntry(day, LocalTime.of(13, 0), LocalTime.of(17, 0)));
                }

                double overtime = workingTimeUtil.getOvertimeForEmployee(employee, projectEntries, List.of(), YearMonth.of(2023, 11));
                assertThat(overtime).isZero();
            }

            @Test
            void when2ndOfNovemberIsAbsenceDay_thenShouldIgnoreAbsentDay() {
                Employee employee = createEmployee().build();
                List<ProjectEntry> projectEntries = new ArrayList<>();

                // Only entry for the 3rd of November, since 1st is a Holiday and 2nd is Absence Day.
                projectEntries.add(createProjectTimeEntry(3, LocalTime.of(8, 0), LocalTime.of(12, 0)));
                projectEntries.add(createProjectTimeEntry(3, LocalTime.of(13, 0), LocalTime.of(17, 0)));

                List<AbsenceTime> fehlzeitTypes = List.of(
                        AbsenceTime.builder()
                                .fromDate(LocalDate.of(2023, 11, 2))
                                .toDate(LocalDate.of(2023, 11, 2))
                                .reason("UB")
                                .accepted(true)
                                .build()
                );

                double overtime = workingTimeUtil.getOvertimeForEmployee(employee, projectEntries, fehlzeitTypes, YearMonth.of(2023, 11));
                assertThat(overtime).isZero();
            }
        }
    }

    @Test
    void getAbsenceTimesForEmployee() {
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

    private List<ProjectTime> returnNormalDayProjectTimes() {
        ProjectTime projektzeitType = ProjectTime.builder().build();
        projektzeitType.setDuration("04:00");
        projektzeitType.setUserId("1");
        projektzeitType.setBillable(false);

        ProjectTime projektzeitTypeBillable = ProjectTime.builder().build();
        projektzeitTypeBillable.setDuration("04:00");
        projektzeitTypeBillable.setUserId("1");
        projektzeitTypeBillable.setBillable(true);


        List<ProjectTime> projectTimes = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            projectTimes.add(projektzeitTypeBillable);
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
                Map.entry(DayOfWeek.TUESDAY, Duration.ofHours(8)),
                Map.entry(DayOfWeek.WEDNESDAY, Duration.ofHours(8)),
                Map.entry(DayOfWeek.THURSDAY, Duration.ofHours(8)),
                Map.entry(DayOfWeek.FRIDAY, Duration.ofHours(8)),
                Map.entry(DayOfWeek.SATURDAY, Duration.ofHours(0)),
                Map.entry(DayOfWeek.SUNDAY, Duration.ofHours(0)));

        return Employee.builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .title("Ing.")
                .userId(user.getUserId())
                .releaseDate("2020-01-01")
                .regularWorkingTimes(new RegularWorkingTimes(new RegularWorkingTime(null, regularWorkingHours)));
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

    private ProjectTimeEntry createProjectTimeEntry(int dayOfMonth, LocalTime timeFrom, LocalTime timeTo) {
        return ProjectTimeEntry.builder()
                .fromTime(LocalDate.of(2023, 11, dayOfMonth).atTime(timeFrom))
                .toTime(LocalDate.of(2023, 11, dayOfMonth).atTime(timeTo))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
                .process("1")
                .build();
    }
}
