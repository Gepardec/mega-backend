package com.gepardec.mega.service.impl.monthlyreport;


import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.service.helper.WorkingTimeFilterHelper;
import de.provantis.zep.FehlzeitType;
import de.provantis.zep.ProjektzeitType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ejb.Local;
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
public class WorkingTimeFilterHelperTest {

    @Inject
    WorkingTimeFilterHelper workingTimeFilterHelper;

    /* Info:
     * returnProjektzeiTypeList has defined entries for a 38.5h week
     *  --> 8h are not billable (fakturierbar)
     *  --> 30.5h are billable (fakturierbar)
     *
     * with counterAddWeek you can define how many weeks are added
     *
     * e.g. for counterAddWeek = 4
     * 38.5 * 4 = 154 total hours
     * 8 * 4    = 32 non billable hours
     * 30.5 * 4 = 122 billable hours
     */


    @Test
    void getInternalTimesForEmployeeTest() {
        Employee employee = createEmployee();

        List<ProjektzeitType> projektzeitTypes = returnProjektzeitTypeList(4);
        String internalTimesForEmployee = workingTimeFilterHelper.getInternalTimesForEmployee(projektzeitTypes, employee);
        assertThat(internalTimesForEmployee).isEqualTo("32:00");
    }
    @Test
    void getBillableTimesForEmployeeTest() {
        Employee employee = createEmployee();

        List<ProjektzeitType> projektzeitTypes = returnProjektzeitTypeList(4);
        String internalTimesForEmployee = workingTimeFilterHelper.getBillableTimesForEmployee(projektzeitTypes, employee);
        assertThat(internalTimesForEmployee).isEqualTo("122:00");
    }
    @Test
    void getTotalWorkingTimeForEmployee() {
        Employee employee = createEmployee();

        List<ProjektzeitType> projektzeitTypes = returnProjektzeitTypeList(4);
        String internalTimesForEmployee = workingTimeFilterHelper.getTotalWorkingTimeForEmployee(projektzeitTypes, employee);
        assertThat(internalTimesForEmployee).isEqualTo("154:00");
    }


    @Test
    void getInternalTimesForEmployee_RETURN_POSITIVE_OVERTIME() {
        Employee employee = createEmployee();

        List<ProjektzeitType> projektzeitTypes = returnProjektzeitTypeList(5);
        double overtimeforEmployee = workingTimeFilterHelper.getOvertimeforEmployee(employee, projektzeitTypes);
        assertThat(overtimeforEmployee).isEqualTo(38.5);
    }
    @Test
    void getInternalTimesForEmployee_RETURN_NEGATIVE_OVERTIME() {
        Employee employee = createEmployee();

        List<ProjektzeitType> projektzeitTypes = returnProjektzeitTypeList(3);
        double overtimeforEmployee = workingTimeFilterHelper.getOvertimeforEmployee(employee, projektzeitTypes);
        assertThat(overtimeforEmployee).isEqualTo(-38.5);
    }
    @Test
    void getAbsenceTimesForEmployee() {
        Employee employee = createEmployee();

        List<FehlzeitType> fehlzeitTypes = returnFehlzeitTypeList();
        int absenceTimesForEmployee = workingTimeFilterHelper.getAbsenceTimesForEmployee(fehlzeitTypes, "UB", LocalDate.of(2021, 7, 1));
        assertThat(absenceTimesForEmployee).isEqualTo(2);
    }


    private List<ProjektzeitType> returnProjektzeitTypeList(int counterAddWeek) {

        List<ProjektzeitType> projektzeiten = new ArrayList<>();

        ProjektzeitType monday = new ProjektzeitType();
        monday.setVon("8:00");
        monday.setBis("16:30");
        monday.setDauer("08:00");
        monday.setUserId("1");
        monday.setIstFakturierbar(false);

        ProjektzeitType tuesday = new ProjektzeitType();
        tuesday.setVon("8:00");
        tuesday.setBis("16:30");
        tuesday.setDauer("08:00");
        tuesday.setUserId("1");
        tuesday.setIstFakturierbar(true);

        ProjektzeitType wednesday = new ProjektzeitType();
        wednesday.setVon("8:00");
        wednesday.setBis("16:30");
        wednesday.setDauer("08:00");
        wednesday.setUserId("1");
        wednesday.setIstFakturierbar(true);

        ProjektzeitType thursday = new ProjektzeitType();
        thursday.setVon("8:00");
        thursday.setBis("16:30");
        thursday.setDauer("08:00");
        thursday.setUserId("1");
        thursday.setIstFakturierbar(true);

        ProjektzeitType friday = new ProjektzeitType();
        friday.setVon("8:00");
        friday.setBis("14:30");
        friday.setDauer("06:30");
        friday.setUserId("1");
        friday.setIstFakturierbar(true);

        for (int i = 0; i < counterAddWeek; i++) {
            projektzeiten.add(monday);
            projektzeiten.add(tuesday);
            projektzeiten.add(wednesday);
            projektzeiten.add(thursday);
            projektzeiten.add(friday);
        }

        return projektzeiten;
    }

    private List<FehlzeitType> returnFehlzeitTypeList(){
        FehlzeitType fehlzeitType = new FehlzeitType();
        fehlzeitType.setStartdatum("2021-07-15");
        fehlzeitType.setEnddatum("2021-07-16");
        fehlzeitType.setFehlgrund("UB");
        fehlzeitType.setUserId("1");
        fehlzeitType.setGenehmigt(true);

        return List.of(fehlzeitType);
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
                        Map.entry(DayOfWeek.TUESDAY, Duration.ofHours(8)),
                        Map.entry(DayOfWeek.WEDNESDAY, Duration.ofHours(8)),
                        Map.entry(DayOfWeek.THURSDAY, Duration.ofHours(8)),
                        Map.entry(DayOfWeek.FRIDAY, Duration.ofHours(6).plusMinutes(30))
                ))
                .build();
    }
}
