package com.gepardec.mega.rest;
import com.gepardec.mega.db.entity.common.PaymentMethodType;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.*;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarning;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.rest.api.WorkerResource;
import com.gepardec.mega.rest.mapper.EmployeeMapper;
import com.gepardec.mega.rest.model.BillDto;
import com.gepardec.mega.rest.model.MappedTimeWarningDTO;
import com.gepardec.mega.rest.model.MonthlyReportDto;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.impl.Rest;
import com.gepardec.mega.zep.impl.Soap;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
@TestSecurity(user = "test")
@JwtSecurity(claims = {
        @Claim(key = "email", value = "test@gepardec.com")
})
public class WorkerResourceTest {

    @InjectMock
    MonthlyReportService monthlyReportService;

    @InjectMock
    EmployeeService employeeService;

    @InjectMock @Rest
    ZepService zepService;

    @InjectMock
    UserContext userContext;

    @InjectMock
    EmployeeMapper mapper;

    @Inject
    WorkerResource workerResource;


    @Test
    void monthlyReport_whenPOST_thenReturnsHttpStatusMETHOD_NOT_ALLOWED() {
        given().contentType(ContentType.JSON)
                .post("/worker/monthendreports")
                .then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void monthlyReport_whenPUT_thenReturnsHttpStatusMETHOD_NOT_ALLOWED() {
        given().contentType(ContentType.JSON)
                .put("/worker/monthendreports")
                .then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void monthlyReport_whenDELETE_thenReturnsHttpStatusMETHOD_NOT_ALLOWED() {
        given().contentType(ContentType.JSON)
                .delete("/worker/monthendreports")
                .then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    @TestSecurity
    @JwtSecurity
    void monthlyReport_whenUserNotLogged_thenReturnsHttpStatusUNAUTHORIZED() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        given().get("/worker/monthendreports")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void monthlyReport_whenGET_thenReturnsMonthlyReport() {
        //GIVEN
        User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        Employee employee = createEmployeeForUser(user);
        when(employeeService.getEmployee(anyString())).thenReturn(employee);

        List<MappedTimeWarningDTO> timeWarnings = List.of();
        List<JourneyWarning> journeyWarnings = List.of();

        int vacationDays = 0;
        int homeofficeDays = 0;
        int compensatoryDays = 0;
        int nursingDays = 0;
        int maternityLeaveDays = 0;
        int externalTrainingDays = 0;
        int conferenceDays = 0;
        int maternityProtectionDays = 0;
        int fatherMonthDays = 0;
        int paidSpecialLeaveDays = 0;
        int nonVacationDays = 0;
        String billableTime = "00:00";
        String totalWorkingTime = "00:00";
        String guildLead = "guildLead";
        String internalProjectLead = "internalProjectLead";

        MonthlyReport expected = MonthlyReport.builder()
                .employee(employee)
                .timeWarnings(timeWarnings)
                .journeyWarnings(journeyWarnings)
                .comments(List.of())
                .employeeCheckState(EmployeeState.OPEN)
                .employeeProgresses(List.of())
                .otherChecksDone(true)
                .billableTime(billableTime)
                .totalWorkingTime(totalWorkingTime)
                .compensatoryDays(compensatoryDays)
                .homeofficeDays(homeofficeDays)
                .vacationDays(vacationDays)
                .nursingDays(nursingDays)
                .maternityLeaveDays(maternityLeaveDays)
                .externalTrainingDays(externalTrainingDays)
                .conferenceDays(conferenceDays)
                .maternityProtectionDays(maternityProtectionDays)
                .fatherMonthDays(fatherMonthDays)
                .paidSpecialLeaveDays(paidSpecialLeaveDays)
                .nonPaidVacationDays(nonVacationDays)
                .guildLead(guildLead)
                .internalProjectLead(internalProjectLead)
                .build();

        when(monthlyReportService.getMonthEndReportForUser()).thenReturn(expected);

        //WHEN
        MonthlyReportDto actual = given().contentType(ContentType.JSON)
                .get("/worker/monthendreports")
                .as(MonthlyReportDto.class);

        //THEN
        assertThat(actual.getEmployee()).isEqualTo(mapper.mapToDto(employee));
        assertThat(timeWarnings).isEqualTo(actual.getTimeWarnings());
        assertThat(journeyWarnings).isEqualTo(actual.getJourneyWarnings());
        assertThat(billableTime).isEqualTo(actual.getBillableTime());
        assertThat(totalWorkingTime).isEqualTo(actual.getTotalWorkingTime());
        assertThat(vacationDays).isEqualTo(actual.getVacationDays());
        assertThat(homeofficeDays).isEqualTo(actual.getHomeofficeDays());
        assertThat(compensatoryDays).isEqualTo(actual.getCompensatoryDays());
        assertThat(nursingDays).isEqualTo(actual.getNursingDays());
        assertThat(maternityLeaveDays).isEqualTo(actual.getMaternityLeaveDays());
        assertThat(externalTrainingDays).isEqualTo(actual.getExternalTrainingDays());
        assertThat(conferenceDays).isEqualTo(actual.getConferenceDays());
        assertThat(maternityProtectionDays).isEqualTo(actual.getMaternityProtectionDays());
        assertThat(fatherMonthDays).isEqualTo(actual.getFatherMonthDays());
        assertThat(paidSpecialLeaveDays).isEqualTo(actual.getPaidSpecialLeaveDays());
        assertThat(nonVacationDays).isEqualTo(actual.getNonPaidVacationDays());
        assertThat(guildLead).isEqualTo(actual.getGuildLead());
        assertThat(internalProjectLead).isEqualTo(actual.getInternalProjectLead());
    }

    @Test
    void monthlyReport_withYearMonth_whenPOST_thenReturnsHttpStatusMETHOD_NOT_ALLOWED() {
        given().contentType(ContentType.JSON)
                .post("/worker/monthendreports/2023/08")
                .then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void monthlyReport_withYearMonth_whenPUT_thenReturnsHttpStatusMETHOD_NOT_ALLOWED() {
        given().contentType(ContentType.JSON)
                .put("/worker/monthendreports/2023/08")
                .then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void monthlyReport_withYearMonth_whenDELETE_thenReturnsHttpStatusMETHOD_NOT_ALLOWED() {
        given().contentType(ContentType.JSON)
                .delete("/worker/monthendreports/2023/08")
                .then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    @TestSecurity
    @JwtSecurity
    void monthlyReport_withYearMonth_whenUserNotLogged_thenReturnsHttpStatusUNAUTHORIZED() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        given().get("/worker/monthendreports/2023/08")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void monthlyReport_withYearMonth_whenGET_thenReturnsMonthlyReport() {
        //GIVEN
        User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        Employee employee = createEmployeeForUser(user);
        when(employeeService.getEmployee(anyString())).thenReturn(employee);

        List<MappedTimeWarningDTO> timeWarnings = List.of();
        List<JourneyWarning> journeyWarnings = List.of();

        int vacationDays = 0;
        int homeofficeDays = 0;
        int compensatoryDays = 0;
        int nursingDays = 0;
        int maternityLeaveDays = 0;
        int externalTrainingDays = 0;
        int conferenceDays = 0;
        int maternityProtectionDays = 0;
        int fatherMonthDays = 0;
        int paidSpecialLeaveDays = 0;
        int nonVacationDays = 0;
        String billableTime = "00:00";
        String totalWorkingTime = "00:00";
        double overtime = 0.0;

        MonthlyReport expected = MonthlyReport.builder()
                .employee(employee)
                .timeWarnings(timeWarnings)
                .journeyWarnings(journeyWarnings)
                .comments(List.of())
                .employeeCheckState(EmployeeState.OPEN)
                .employeeProgresses(List.of())
                .otherChecksDone(true)
                .billableTime(billableTime)
                .totalWorkingTime(totalWorkingTime)
                .compensatoryDays(compensatoryDays)
                .homeofficeDays(homeofficeDays)
                .vacationDays(vacationDays)
                .nursingDays(nursingDays)
                .maternityLeaveDays(maternityLeaveDays)
                .externalTrainingDays(externalTrainingDays)
                .conferenceDays(conferenceDays)
                .maternityProtectionDays(maternityProtectionDays)
                .fatherMonthDays(fatherMonthDays)
                .paidSpecialLeaveDays(paidSpecialLeaveDays)
                .nonPaidVacationDays(nonVacationDays)
                .overtime(overtime)
                .build();

        when(monthlyReportService.getMonthEndReportForUser(2023, 8, null, null)).thenReturn(expected);

        //WHEN
        MonthlyReportDto actual = given().contentType(ContentType.JSON)
                .get("/worker/monthendreports/2023/08")
                .as(MonthlyReportDto.class);

        //THEN
        assertThat(actual.getEmployee()).isEqualTo(mapper.mapToDto(employee));
        assertThat(timeWarnings).isEqualTo(actual.getTimeWarnings());
        assertThat(journeyWarnings).isEqualTo(actual.getJourneyWarnings());
        assertThat(billableTime).isEqualTo(actual.getBillableTime());
        assertThat(totalWorkingTime).isEqualTo(actual.getTotalWorkingTime());
        assertThat(vacationDays).isEqualTo(actual.getVacationDays());
        assertThat(homeofficeDays).isEqualTo(actual.getHomeofficeDays());
        assertThat(compensatoryDays).isEqualTo(actual.getCompensatoryDays());
        assertThat(nursingDays).isEqualTo(actual.getNursingDays());
        assertThat(maternityLeaveDays).isEqualTo(actual.getMaternityLeaveDays());
        assertThat(externalTrainingDays).isEqualTo(actual.getExternalTrainingDays());
        assertThat(conferenceDays).isEqualTo(actual.getConferenceDays());
        assertThat(maternityProtectionDays).isEqualTo(actual.getMaternityProtectionDays());
        assertThat(fatherMonthDays).isEqualTo(actual.getFatherMonthDays());
        assertThat(paidSpecialLeaveDays).isEqualTo(actual.getPaidSpecialLeaveDays());
        assertThat(nonVacationDays).isEqualTo(actual.getNonPaidVacationDays());
    }


    @Test
    void getBillsForEmployeeByMonth_whenEmployeeHasBills_thenReturnBills(){
        User userForRole = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(userForRole);
        final Employee userAsEmployee = createEmployeeForUser(userForRole);


        when(employeeService.getEmployee(userAsEmployee.getUserId()))
                .thenReturn(userAsEmployee);


       /* when(zepService.getBillsForEmployeeByMonth(any(Employee.class), any(YearMonth.class)))
                .thenReturn(
                    getBillsForEmployee()
                );*/

        doReturn(getBillsForEmployee()).when(zepService).getBillsForEmployeeByMonth(any(Employee.class), any(YearMonth.class));

        List<BillDto> actual = workerResource.getBillsForEmployeeByMonth(userAsEmployee.getUserId(), YearMonth.of(2024, 4));

        assertThat(actual).isNotNull().size().isEqualTo(3);
        assertThat(actual.get(0).getBillType()).isEqualTo("Lebensmittel");
    }

    @Test
    void getBillsForEmployeeByMonth_whenEmployeeHasNoBills_thenReturnEmptyList(){
        User userForRole = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(userForRole);
        final Employee userAsEmployee = createEmployeeForUser(userForRole);


        when(employeeService.getEmployee(userAsEmployee.getUserId()))
                .thenReturn(userAsEmployee);


        when(zepService.getBillsForEmployeeByMonth(userAsEmployee, YearMonth.of(2024, 4)))
                .thenReturn(
                        List.of()
                );

        List<BillDto> actual = workerResource.getBillsForEmployeeByMonth(userAsEmployee.getUserId(), YearMonth.of(2024, 4));

        assertThat(actual).isNotNull();
        assertThat(actual).isEmpty();
    }

    private Bill createBillForEmployee(LocalDate billDate, double bruttoValue, String billType,
                                       PaymentMethodType paymentMethodType, String projectName,
                                       String attachmentBase64String){
        return Bill.builder()
                .billDate(billDate)
                .bruttoValue(bruttoValue)
                .billType(billType)
                .paymentMethodType(paymentMethodType)
                .projectName(projectName)
                .attachmentBase64(attachmentBase64String)
                .build();
    }

    private List<Bill> getBillsForEmployee() {
        return List.of(
                createBillForEmployee(LocalDate.of(2024, 4, 11),
                        12.0,
                        "Lebensmittel",
                        PaymentMethodType.COMPANY,
                        "3BankenIT - JBoss",
                        null),
                createBillForEmployee(LocalDate.of(2024, 4, 10),
                        12.0,
                        "Büromaterial",
                        PaymentMethodType.PRIVATE,
                        "3BankenIT - JBoss",
                        null),
                createBillForEmployee(LocalDate.of(2024, 4, 9),
                        30.0,
                        "Lebensmittel",
                        PaymentMethodType.PRIVATE,
                        "3BankenIT - JBoss",
                        null)
        );
    }
    private Employee createEmployeeForUser(final User user) {
        return Employee.builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .title("Ing.")
                .userId(user.getUserId())
                .releaseDate("2020-01-01")
                .active(true)
                .build();
    }

    private User createUserForRole(final Role role) {
        return User.builder()
                .dbId(1)
                .userId("1")
                .email("max.mustermann@gpeardec.com")
                .firstname("Max")
                .lastname("Mustermann")
                .roles(Set.of(role))
                .build();
    }
}
