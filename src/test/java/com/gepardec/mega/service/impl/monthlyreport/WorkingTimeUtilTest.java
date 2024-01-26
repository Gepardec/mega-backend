package com.gepardec.mega.service.impl.monthlyreport;


import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.service.helper.WorkingTimeUtil;
import de.provantis.zep.FehlzeitType;
import de.provantis.zep.ProjektzeitType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
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
        Employee employee = createEmployee().build();

        List<ProjektzeitType> projektzeitTypes = returnNormalDayProjektzeitTypes(5);
        String internalTimesForEmployee = workingTimeUtil.getInternalTimesForEmployee(projektzeitTypes, employee);
        assertThat(internalTimesForEmployee).isEqualTo("20:00");
    }

    @Test
    void getBillableTimesForEmployeeTest() {
        Employee employee = createEmployee().build();

        List<ProjektzeitType> projektzeitTypes = returnNormalDayProjektzeitTypes(5);
        String internalTimesForEmployee = workingTimeUtil.getBillableTimesForEmployee(projektzeitTypes, employee);
        assertThat(internalTimesForEmployee).isEqualTo("20:00");
    }

    @Test
    void getTotalWorkingTimeForEmployee() {
        Employee employee = createEmployee().build();

        List<ProjektzeitType> projektzeitTypes = returnNormalDayProjektzeitTypes(5);
        String internalTimesForEmployee = workingTimeUtil.getTotalWorkingTimeForEmployee(projektzeitTypes, employee);
        assertThat(internalTimesForEmployee).isEqualTo("40:00");
    }

    @Test
    void getOvertimeForEmployee_RETURN_POSITIVE_OVERTIME() {
        Employee employee = createEmployee().build();

        List<ProjektzeitType> projektzeitTypes = returnNormalDayProjektzeitTypes(5);
        List<FehlzeitType> fehlzeitTypes = List.of();

        double overtimeforEmployee = workingTimeUtil.getOvertimeForEmployee(employee, projektzeitTypes, fehlzeitTypes, LocalDate.of(2023, 11, 1));
        assertThat(overtimeforEmployee).isEqualTo(8.0);
    }

    @Test
    void getOvertimeForEmployee_RETURN_NEGATIVE_OVERTIME() {
        Employee employee = createEmployee().build();

        List<ProjektzeitType> projektzeitTypes = returnNormalDayProjektzeitTypes(3);
        List<FehlzeitType> fehlzeitTypes = List.of();

        double overtimeforEmployee = workingTimeUtil.getOvertimeForEmployee(employee, projektzeitTypes, fehlzeitTypes, LocalDate.of(2023, 11, 1));
        assertThat(overtimeforEmployee).isEqualTo(-8.);
    }

    @Test
    void getOvertimeForEmployee_WITH_ABSENCE() {
        Employee employee = createEmployee().build();

        List<ProjektzeitType> projektzeitTypes = returnNormalDayProjektzeitTypes(3);
        List<FehlzeitType> fehlzeitTypes = returnFehlzeitTypeList();

        double overtimeforEmployee = workingTimeUtil.getOvertimeForEmployee(employee, projektzeitTypes, fehlzeitTypes, LocalDate.of(2023, 11, 1));
        assertThat(overtimeforEmployee).isEqualTo(0);
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
        Employee employee = createEmployee().regularWorkingHours(regularWorkingHours).build();

        List<ProjektzeitType> projektzeitTypes = returnNormalDayProjektzeitTypes(3);
        List<FehlzeitType> fehlzeitTypes = returnFehlzeitTypeList();

        double overtimeforEmployee = workingTimeUtil.getOvertimeForEmployee(
                employee,
                projektzeitTypes,
                fehlzeitTypes,
                LocalDate.of(2023, 10, 1)
        );
        assertThat(overtimeforEmployee).isEqualTo(0);
    }

    @Test
    void getAbsenceTimesForEmployee() {
        Employee employee = createEmployee().build();

        List<FehlzeitType> fehlzeitTypes = returnFehlzeitTypeList();
        int absenceTimesForEmployee = workingTimeUtil.getAbsenceTimesForEmployee(fehlzeitTypes, "UB", LocalDate.of(2023, 11, 6));
        assertThat(absenceTimesForEmployee).isEqualTo(2);
    }

    private List<FehlzeitType> returnFehlzeitTypeList() {
        FehlzeitType fehlzeitType = new FehlzeitType();
        fehlzeitType.setStartdatum("2023-11-06");
        fehlzeitType.setEnddatum("2023-11-07");
        fehlzeitType.setFehlgrund("UB");
        fehlzeitType.setUserId("1");
        fehlzeitType.setGenehmigt(true);

        return List.of(fehlzeitType);
    }

    private List<ProjektzeitType> returnNormalDayProjektzeitTypes(int times) {
        ProjektzeitType projektzeitType = new ProjektzeitType();
        projektzeitType.setVon("8:00");
        projektzeitType.setBis("12:00");
        projektzeitType.setDauer("04:00");
        projektzeitType.setUserId("1");
        projektzeitType.setIstFakturierbar(false);

        ProjektzeitType projektzeitTypeBilllable = new ProjektzeitType();
        projektzeitTypeBilllable.setVon("12:00");
        projektzeitTypeBilllable.setBis("16:00");
        projektzeitTypeBilllable.setDauer("04:00");
        projektzeitTypeBilllable.setUserId("1");
        projektzeitTypeBilllable.setIstFakturierbar(true);


        List<ProjektzeitType> projektzeitTypes = new ArrayList<>();

        for (int i = 0; i < times; i++) {
            projektzeitTypes.add(projektzeitTypeBilllable);
            projektzeitTypes.add(projektzeitType);
        }
        return projektzeitTypes;
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
                );
    }
}
