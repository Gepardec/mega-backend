package com.gepardec.mega.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarning;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.rest.mapper.EmployeeMapper;
import com.gepardec.mega.rest.model.MappedTimeWarningDTO;
import com.gepardec.mega.rest.model.MonthlyReportDto;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.MonthlyReportService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import jakarta.inject.Inject;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

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

    @InjectMock
    UserContext userContext;

    @InjectMock
    EmployeeMapper mapper;

    @Inject
    ObjectMapper objectMapper;

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

        MonthlyReport expected = MonthlyReport.builder()
                .employee(employee)
                .timeWarnings(timeWarnings)
                .journeyWarnings(journeyWarnings)
                .comments(List.of())
                .employeeCheckState(EmployeeState.OPEN)
                .isAssigned(false)
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
                .build();

        when(monthlyReportService.getMonthEndReportForUser()).thenReturn(expected);

        MonthlyReportDto actual = given().contentType(ContentType.JSON)
                .get("/worker/monthendreports")
                .as(MonthlyReportDto.class);


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
                .isAssigned(false)
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

        when(monthlyReportService.getMonthEndReportForUser(2023, 8, null)).thenReturn(expected);


        RestAssured.defaultParser = Parser.JSON;

        MonthlyReportDto actual = given().contentType(ContentType.JSON)
                .get("/worker/monthendreports/2023/08")
                .as(MonthlyReportDto.class);

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
