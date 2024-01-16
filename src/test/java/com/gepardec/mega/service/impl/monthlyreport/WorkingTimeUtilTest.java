package com.gepardec.mega.service.impl.monthlyreport;


import com.gepardec.mega.domain.model.*;
import com.gepardec.mega.service.helper.WorkingTimeUtil;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@QuarkusTest
public class WorkingTimeUtilTest {

    @Inject
    WorkingTimeUtil workingTimeUtil;

    @Test
    void getInternalTimesForEmployeeTest() {
        Employee employee = createEmployee();

        List<ProjectTime> projectTimes = returnNormalDayProjectTimes(5);
        String internalTimesForEmployee = workingTimeUtil.getInternalTimesForEmployee(projectTimes, employee);
        assertThat(internalTimesForEmployee).isEqualTo("20:00");
    }

    @Test
    void getBillableTimesForEmployeeTest() {
        Employee employee = createEmployee();

        List<ProjectTime> projectTimes = returnNormalDayProjectTimes(5);
        String internalTimesForEmployee = workingTimeUtil.getBillableTimesForEmployee(projectTimes, employee);
        assertThat(internalTimesForEmployee).isEqualTo("20:00");
    }

    @Test
    void getTotalWorkingTimeForEmployee() {
        Employee employee = createEmployee();

        List<ProjectTime> projectTimes = returnNormalDayProjectTimes(5);
        String internalTimesForEmployee = workingTimeUtil.getTotalWorkingTimeForEmployee(projectTimes, employee);
        assertThat(internalTimesForEmployee).isEqualTo("40:00");
    }

    @Test
    void getOvertimeForEmployee_RETURN_POSITIVE_OVERTIME() {
        Employee employee = createEmployee();

        List<ProjectTime> projectTimes = returnNormalDayProjectTimes(5);
        List<AbsenceTime> fehlzeitTypes = List.of();

        double overtimeforEmployee = workingTimeUtil.getOvertimeForEmployee(employee, projectTimes, fehlzeitTypes, LocalDate.of(2023, 11, 1));
        assertThat(overtimeforEmployee).isEqualTo(8.0);
    }

    @Test
    void getOvertimeForEmployee_RETURN_NEGATIVE_OVERTIME() {
        Employee employee = createEmployee();

        List<ProjectTime> projectTimes = returnNormalDayProjectTimes(3);
        List<AbsenceTime> fehlzeitTypes = List.of();

        double overtimeforEmployee = workingTimeUtil.getOvertimeForEmployee(employee, projectTimes, fehlzeitTypes, LocalDate.of(2023, 11, 1));
        assertThat(overtimeforEmployee).isEqualTo(-8.);
    }

    @Test
    void getOvertimeForEmployee_WITH_ABSENCE() {
        Employee employee = createEmployee();

        List<ProjectTime> projectTimes = returnNormalDayProjectTimes(3);
        List<AbsenceTime> fehlzeitTypes = returnFehlzeitTypeList();

        double overtimeforEmployee = workingTimeUtil.getOvertimeForEmployee(employee, projectTimes, fehlzeitTypes, LocalDate.of(2023, 11, 1));
        assertThat(overtimeforEmployee).isEqualTo(0);
    }

    @Test
    void getOvertimeForEmployee_WITH_HOLIDAY() {
        Employee employee = createEmployee();
        HashMap<DayOfWeek, Duration> regularWorkingHours = new HashMap<>(employee.getRegularWorkingHours());
        regularWorkingHours.put(DayOfWeek.MONDAY, Duration.ofHours(0));
        regularWorkingHours.put(DayOfWeek.THURSDAY, Duration.ofHours(8));
        employee.setRegularWorkingHours(regularWorkingHours);

        List<ProjectTime> projectTimes = returnNormalDayProjectTimes(3);
        List<AbsenceTime> fehlzeitTypes = returnFehlzeitTypeList();

        double overtimeforEmployee = workingTimeUtil.getOvertimeForEmployee(
                employee,
                projectTimes,
                fehlzeitTypes,
                LocalDate.of(2023, 10, 1)
        );
        assertThat(overtimeforEmployee).isEqualTo(0);
    }

    @Test
    void getAbsenceTimesForEmployee() {
        Employee employee = createEmployee();

        List<AbsenceTime> fehlzeitTypes = returnFehlzeitTypeList();
        int absenceTimesForEmployee = workingTimeUtil.getAbsenceTimesForEmployee(fehlzeitTypes, "UB", LocalDate.of(2023, 11, 6));
        assertThat(absenceTimesForEmployee).isEqualTo(2);
    }

    private List<AbsenceTime> returnFehlzeitTypeList() {
        AbsenceTime fehlzeitType = new AbsenceTime();
        fehlzeitType.setFromDate(LocalDate.of(2023, 11, 6));
        fehlzeitType.setToDate(LocalDate.of(2023,11,7));
        fehlzeitType.setReason("UB");
        fehlzeitType.setUserId("1");
        fehlzeitType.setAccepted(true);

        return List.of(fehlzeitType);
    }

    private List<ProjectTime> returnNormalDayProjectTimes(int times) {
        ProjectTime projektzeitType = new ProjectTime();
        projektzeitType.setStartTime("8:00");
        projektzeitType.setEndTime("12:00");
        projektzeitType.setDuration("04:00");
        projektzeitType.setUserId("1");
        projektzeitType.setIsBillable(false);

        ProjectTime projektzeitTypeBilllable = new ProjectTime();
        projektzeitTypeBilllable.setStartTime("12:00");
        projektzeitTypeBilllable.setEndTime("16:00");
        projektzeitTypeBilllable.setDuration("04:00");
        projektzeitTypeBilllable.setUserId("1");
        projektzeitTypeBilllable.setIsBillable(true);


        List<ProjectTime> projectTimes = new ArrayList<>();

        for (int i = 0; i < times; i++) {
            projectTimes.add(projektzeitTypeBilllable);
            projectTimes.add(projektzeitType);
        }
        return projectTimes;
    }

    private Employee createEmployee() {
        User user = User.builder()
                .dbId(1)
                .userId("1")
                .email("max.mustermann@gpeardec.com")
                .firstname("Max")
                .lastname("Mustermann")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        return Employee.builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .title("Ing.")
                .userId(user.getUserId())
                .releaseDate("2020-01-01")
                .active(true)
                .regularWorkingHours(Map.ofEntries(
                        Map.entry(DayOfWeek.MONDAY, Duration.ofHours(8)),
                        Map.entry(DayOfWeek.TUESDAY, Duration.ofHours(0)),
                        Map.entry(DayOfWeek.WEDNESDAY, Duration.ofHours(0)),
                        Map.entry(DayOfWeek.THURSDAY, Duration.ofHours(0)),
                        Map.entry(DayOfWeek.FRIDAY, Duration.ofHours(0)),
                        Map.entry(DayOfWeek.SATURDAY, Duration.ofHours(0)),
                        Map.entry(DayOfWeek.SUNDAY, Duration.ofHours(0)))
                )
                .build();
    }
}
