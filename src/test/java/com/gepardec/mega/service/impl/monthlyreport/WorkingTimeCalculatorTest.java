package com.gepardec.mega.service.impl.monthlyreport;


import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.service.helper.WorkingTimeCalculator;
import com.gepardec.mega.zep.ZepService;
import de.provantis.zep.ProjektzeitType;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
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
public class WorkingTimeCalculatorTest {

    @Inject
    WorkingTimeCalculator workingTimeCalculator;

    @Test
    void getInternalTimesForEmployee_RETURN_ONLY_INTERNAL() {
        Employee employee = createEmployee();

        List<ProjektzeitType> projektzeitTypes = returnProjektzeitTypeList();
        String internalTimesForEmployee = workingTimeCalculator.getInternalTimesForEmployee(projektzeitTypes, employee);
        assertThat(internalTimesForEmployee).isEqualTo("08:00");
    }


    @Test
    void getInternalTimesForEmployee_RETURN_OVERTIME() {
        Employee employee = createEmployee();

        List<ProjektzeitType> projektzeitTypes = returnProjektzeitTypeList();
        double overtimeforEmployee = workingTimeCalculator.getOvertimeforEmployee(employee, projektzeitTypes);
        assertThat(overtimeforEmployee).isEqualTo(12.0);
    }


    private List<ProjektzeitType> returnProjektzeitTypeList() {

        List<ProjektzeitType> projektzeiten = new ArrayList<>();

        ProjektzeitType projektzeitType1 = new ProjektzeitType();
        projektzeitType1.setVon("12:30");
        projektzeitType1.setBis("16:30");
        projektzeitType1.setDauer("04:00");
        projektzeitType1.setUserId("1");
        projektzeitType1.setIstFakturierbar(true);

        ProjektzeitType projektzeitType2 = new ProjektzeitType();
        projektzeitType2.setVon("12:30");
        projektzeitType2.setBis("16:30");
        projektzeitType2.setDauer("04:00");
        projektzeitType2.setUserId("1");
        projektzeitType2.setIstFakturierbar(true);

        ProjektzeitType projektzeitType3 = new ProjektzeitType();
        projektzeitType3.setVon("12:30");
        projektzeitType3.setBis("16:30");
        projektzeitType3.setDauer("04:00");
        projektzeitType3.setUserId("1");
        projektzeitType3.setIstFakturierbar(false);

        ProjektzeitType projektzeitType4 = new ProjektzeitType();
        projektzeitType4.setVon("12:30");
        projektzeitType4.setBis("16:30");
        projektzeitType4.setDauer("04:00");
        projektzeitType4.setUserId("1");
        projektzeitType4.setIstFakturierbar(false);


        projektzeiten.add(projektzeitType1);
        projektzeiten.add(projektzeitType2);
        projektzeiten.add(projektzeitType3);
        projektzeiten.add(projektzeitType4);
        return projektzeiten;
    }

    private Employee createEmployee() {


        User user = User.builder().dbId(1).userId("1").email("max.mustermann@gpeardec.com").firstname("Max").lastname("Mustermann").roles(Set.of(Role.EMPLOYEE)).build();

        return Employee.builder().email(user.getEmail()).firstname(user.getFirstname()).lastname(user.getLastname()).title("Ing.").userId(user.getUserId()).releaseDate("2020-01-01").active(true).regularWorkingHours(Map.of(DayOfWeek.MONDAY, Duration.ofHours(4))).build();
    }
}
