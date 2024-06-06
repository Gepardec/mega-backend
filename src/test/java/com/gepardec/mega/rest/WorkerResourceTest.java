package com.gepardec.mega.rest;
import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.MonthlyAbsences;
import com.gepardec.mega.domain.model.MonthlyBillInfo;
import com.gepardec.mega.domain.model.MonthlyWarning;
import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.domain.model.ProjectHoursSummary;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.domain.model.monthlyreport.JourneyDirection;
import com.gepardec.mega.domain.model.monthlyreport.JourneyTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarning;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.Task;
import com.gepardec.mega.domain.model.monthlyreport.Vehicle;
import com.gepardec.mega.domain.model.monthlyreport.WorkingLocation;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.personio.employees.PersonioEmployeesService;
import com.gepardec.mega.rest.api.WorkerResource;
import com.gepardec.mega.rest.mapper.EmployeeMapper;
import com.gepardec.mega.rest.mapper.MonthlyAbsencesMapper;
import com.gepardec.mega.rest.mapper.MonthlyBillInfoMapper;
import com.gepardec.mega.rest.mapper.MonthlyWarningMapper;
import com.gepardec.mega.rest.model.MappedTimeWarningDTO;
import com.gepardec.mega.rest.model.MonthlyAbsencesDto;
import com.gepardec.mega.rest.model.MonthlyBillInfoDto;
import com.gepardec.mega.rest.model.MonthlyOfficeDaysDto;
import com.gepardec.mega.rest.model.MonthlyReportDto;
import com.gepardec.mega.rest.model.MonthlyWarningDto;
import com.gepardec.mega.rest.model.ProjectHoursSummaryDto;
import com.gepardec.mega.service.api.AbsenceService;
import com.gepardec.mega.service.api.DateHelperService;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.service.api.TimeWarningService;
import com.gepardec.mega.service.helper.WorkingTimeUtil;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.impl.Rest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    WorkingTimeUtil workingTimeUtil;

    @InjectMock
    AbsenceService absenceService;

    @InjectMock
    TimeWarningService timeWarningService;

    @InjectMock
    MonthlyBillInfoMapper monthlyBillInfoMapper;

    @InjectMock
    PersonioEmployeesService personioEmployeesService;

    @InjectMock @Rest
    ZepService zepService;

    @InjectMock
    UserContext userContext;

    @InjectMock
    EmployeeMapper mapper;

    @InjectMock
    MonthlyAbsencesMapper monthlyAbsencesMapper;

    @Inject
    MonthlyWarningMapper monthlyWarningMapper;

    @InjectMock
    DateHelperService dateHelperService;

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
    void getMonthlyBillInfoForEmployeeByMonth_whenEmployeeHasBillsWithoutAttachmentAndCreditCard_thenReturnObjectWithAttachmentWarningsAndNoCreditCard(){
        User userForRole = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(userForRole);
        final Employee userAsEmployee = createEmployeeForUser(userForRole);
        PersonioEmployee personioEmployee = PersonioEmployee.builder().hasCreditCard(false).build();

        when(employeeService.getEmployee(userAsEmployee.getUserId()))
                .thenReturn(userAsEmployee);

        when(personioEmployeesService.getPersonioEmployeeByEmail(anyString()))
                .thenReturn(Optional.of(personioEmployee));

       when(zepService.getMonthlyBillInfoForEmployee(any(PersonioEmployee.class), any(Employee.class), any(YearMonth.class)))
               .thenReturn(createMonthlyBillInfo(2, 1, 1, true, false));

       when(monthlyBillInfoMapper.mapToDto(any(MonthlyBillInfo.class)))
               .thenReturn(createMonthlyBillInfoDto(2, 1, 1, true, false));

        MonthlyBillInfoDto actual = workerResource.getBillInfoForEmployee(userAsEmployee.getUserId(), YearMonth.of(2024, 4));

        assertThat(actual.getEmployeeHasCreditCard()).isFalse();
        assertThat(actual.getSumBills()).isEqualTo(2);
        assertThat(actual.getSumPrivateBills()).isOne();
        assertThat(actual.getSumCompanyBills()).isOne();
        assertThat(actual.getHasAttachmentWarnings()).isTrue();
    }

    @Test
    void getMonthlyBillInfoForEmployeeByMonth_whenEmployeeHasBillsWithAllAttachmentsAndNoCreditCard_thenReturnObjectWithoutAttachmentWarningsAndNoCreditCard(){
        User userForRole = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(userForRole);
        final Employee userAsEmployee = createEmployeeForUser(userForRole);
        PersonioEmployee personioEmployee = PersonioEmployee.builder().hasCreditCard(false).build();

        when(employeeService.getEmployee(userAsEmployee.getUserId()))
                .thenReturn(userAsEmployee);

        when(personioEmployeesService.getPersonioEmployeeByEmail(anyString()))
                .thenReturn(Optional.of(personioEmployee));

        when(zepService.getMonthlyBillInfoForEmployee(any(PersonioEmployee.class), any(Employee.class), any(YearMonth.class)))
                .thenReturn(createMonthlyBillInfo(3, 2, 1, false, false));

        when(monthlyBillInfoMapper.mapToDto(any(MonthlyBillInfo.class)))
                .thenReturn(createMonthlyBillInfoDto(3, 2, 1, false, false));

        MonthlyBillInfoDto actual = workerResource.getBillInfoForEmployee(userAsEmployee.getUserId(), YearMonth.of(2024, 4));

        assertThat(actual.getEmployeeHasCreditCard()).isFalse();
        assertThat(actual.getSumBills()).isEqualTo(3);
        assertThat(actual.getSumPrivateBills()).isEqualTo(2);
        assertThat(actual.getSumCompanyBills()).isOne();
        assertThat(actual.getHasAttachmentWarnings()).isFalse();
    }

    @Test
    void getMonthlyBillInfoForEmployeeByMonth_whenEmployeeHasBillsWithAllAttachmentsAndCreditCard_thenReturnObjectWithoutAttachmentWarningsAndWithCreditCard(){
        User userForRole = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(userForRole);
        final Employee userAsEmployee = createEmployeeForUser(userForRole);
        PersonioEmployee personioEmployee = PersonioEmployee.builder().hasCreditCard(false).build();

        when(employeeService.getEmployee(userAsEmployee.getUserId()))
                .thenReturn(userAsEmployee);

        when(personioEmployeesService.getPersonioEmployeeByEmail(anyString()))
                .thenReturn(Optional.of(personioEmployee));

        when(zepService.getMonthlyBillInfoForEmployee(any(PersonioEmployee.class), any(Employee.class), any(YearMonth.class)))
                .thenReturn(createMonthlyBillInfo(3, 1, 2, false, true));

        when(monthlyBillInfoMapper.mapToDto(any(MonthlyBillInfo.class)))
                .thenReturn(createMonthlyBillInfoDto(3, 1, 2, false, true));

        MonthlyBillInfoDto actual = workerResource.getBillInfoForEmployee(userAsEmployee.getUserId(), YearMonth.of(2024, 4));

        assertThat(actual.getEmployeeHasCreditCard()).isTrue();
        assertThat(actual.getSumBills()).isEqualTo(3);
        assertThat(actual.getSumPrivateBills()).isOne();
        assertThat(actual.getSumCompanyBills()).isEqualTo(2);
        assertThat(actual.getHasAttachmentWarnings()).isFalse();
    }

    @Test
    void getBillsForEmployeeByMonth_whenEmployeeHasNoBills_thenReturnObjectWithSumZero(){
        User userForRole = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(userForRole);
        final Employee userAsEmployee = createEmployeeForUser(userForRole);
        PersonioEmployee personioEmployee = PersonioEmployee.builder().hasCreditCard(false).build();

        when(employeeService.getEmployee(userAsEmployee.getUserId()))
                .thenReturn(userAsEmployee);

        when(personioEmployeesService.getPersonioEmployeeByEmail(anyString()))
                .thenReturn(Optional.of(personioEmployee));

        when(zepService.getMonthlyBillInfoForEmployee(any(PersonioEmployee.class), any(Employee.class), any(YearMonth.class)))
                .thenReturn(createMonthlyBillInfo(0, 0, 0, false, true));

        when(monthlyBillInfoMapper.mapToDto(any(MonthlyBillInfo.class)))
                .thenReturn(createMonthlyBillInfoDto(0, 0, 0, false, true));

        MonthlyBillInfoDto actual = workerResource.getBillInfoForEmployee(userAsEmployee.getUserId(), YearMonth.of(2024, 4));

        assertThat(actual.getEmployeeHasCreditCard()).isTrue();
        assertThat(actual.getSumBills()).isZero();
        assertThat(actual.getSumPrivateBills()).isZero();
        assertThat(actual.getSumCompanyBills()).isZero();
        assertThat(actual.getHasAttachmentWarnings()).isFalse();
    }
    @Test
    void getAllProjectsForMonthAndEmployee_whenEmployeeHasProjectTimes_thenReturnListOfProjects() {
        User userForRole = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(userForRole);
        final Employee userAsEmployee = createEmployeeForUser(userForRole);


        when(employeeService.getEmployee(anyString()))
                .thenReturn(userAsEmployee);

        when(zepService.getAllProjectsForMonthAndEmployee(any(Employee.class), any(YearMonth.class)))
                .thenReturn(getProjectsHoursSummaryForEmployee());

        List<ProjectHoursSummaryDto> actual = workerResource.getAllProjectsForMonthAndEmployee(userAsEmployee.getUserId(), YearMonth.of(2024, 6));

        assertThat(actual).isNotNull();
        assertThat(actual.size()).isEqualTo(4);
    }

    @Test
    void getAllProjectsForMonthAndEmployee_whenEmployeeHasNoProjectTimes_thenReturnListOf() {
        User userForRole = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(userForRole);
        final Employee userAsEmployee = createEmployeeForUser(userForRole);


        when(employeeService.getEmployee(anyString()))
                .thenReturn(userAsEmployee);

        when(zepService.getAllProjectsForMonthAndEmployee(any(Employee.class), any(YearMonth.class)))
                .thenReturn(List.of());

        List<ProjectHoursSummaryDto> actual = workerResource.getAllProjectsForMonthAndEmployee(userAsEmployee.getUserId(), YearMonth.of(2024, 6));

        assertThat(actual).isNotNull();
        assertThat(actual.size()).isZero();
    }

    @Test
    void getAllAbsencesForMonthAndEmployee_whenEmployeeHasAbsences_thenReturnAbsenceObjectWithValues() {
        User userForRole = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(userForRole);
        final Employee userAsEmployee = createEmployeeForUser(userForRole);

        int availableVacationDays = 20;
        double doctorsVisitingTime = 1.75;
        LocalDate fromDate = DateUtils.getFirstDayOfCurrentMonth(LocalDate.now().toString());
        String toDateString = DateUtils.getLastDayOfCurrentMonth(fromDate);
        Pair<String, String> datePair = Pair.of(fromDate.toString(), toDateString);


        when(employeeService.getEmployee(anyString()))
                .thenReturn(userAsEmployee);

        when(personioEmployeesService.getAvailableVacationDaysForEmployeeByEmail(anyString()))
                .thenReturn(availableVacationDays);

        when(zepService.getDoctorsVisitingTimeForMonthAndEmployee(any(Employee.class), any(YearMonth.class)))
                .thenReturn(doctorsVisitingTime);

        when(dateHelperService.getCorrectDateForRequest(any(Employee.class), any(YearMonth.class)))
                .thenReturn(datePair);

        when(zepService.getAbsenceForEmployee(any(Employee.class), any(LocalDate.class)))
                .thenReturn(createAbsenceListForEmployee());

        when(monthlyAbsencesMapper.mapToDto(any(MonthlyAbsences.class)))
                .thenReturn(
                        MonthlyAbsencesDto.builder()
                                .availableVacationDays(availableVacationDays)
                                .doctorsVisitingTime(doctorsVisitingTime)
                                .conferenceDays(1)
                                .vacationDays(1)
                                .maternityLeaveDays(1)
                                .paidSickLeave(1)
                                .build()
                );

        MonthlyAbsencesDto actual = workerResource.getAllAbsencesForMonthAndEmployee(userAsEmployee.getUserId(), YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth()));

        assertThat(actual.getAvailableVacationDays()).isEqualTo(availableVacationDays);
        assertThat(actual.getDoctorsVisitingTime()).isEqualTo(doctorsVisitingTime);
        assertThat(actual.getConferenceDays()).isOne();
    }

    @Test
    void getAllAbsencesForMonthAndEmployee_whenEmployeeHasNoAbsences_thenReturnAbsenceObjectWithAllZeros() {
        User userForRole = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(userForRole);
        final Employee userAsEmployee = createEmployeeForUser(userForRole);
        int availableVacationDays = 0;
        double doctorsVisitingTime = 0.0;
        LocalDate fromDate = DateUtils.getFirstDayOfCurrentMonth(LocalDate.now().toString());
        String toDateString = DateUtils.getLastDayOfCurrentMonth(fromDate);
        Pair<String, String> datePair = Pair.of(fromDate.toString(), toDateString);


        when(employeeService.getEmployee(anyString()))
                .thenReturn(userAsEmployee);

        when(personioEmployeesService.getAvailableVacationDaysForEmployeeByEmail(anyString()))
                .thenReturn(availableVacationDays);

        when(zepService.getDoctorsVisitingTimeForMonthAndEmployee(any(Employee.class), any(YearMonth.class)))
                .thenReturn(doctorsVisitingTime);

        when(dateHelperService.getCorrectDateForRequest(any(Employee.class), any(YearMonth.class)))
                .thenReturn(datePair);

        when(zepService.getAbsenceForEmployee(any(Employee.class), any(LocalDate.class)))
                .thenReturn(createAbsenceListForEmployeeWithNoAbsences());


        when(monthlyAbsencesMapper.mapToDto(any(MonthlyAbsences.class)))
                .thenReturn(
                        MonthlyAbsencesDto.builder()
                                .availableVacationDays(availableVacationDays)
                                .doctorsVisitingTime(doctorsVisitingTime)
                                .vacationDays(0)
                                .compensatoryDays(0)
                                .nursingDays(0)
                                .maternityLeaveDays(0)
                                .externalTrainingDays(0)
                                .conferenceDays(0)
                                .maternityProtectionDays(0)
                                .fatherMonthDays(0)
                                .paidSpecialLeaveDays(0)
                                .nonPaidVacationDays(0)
                                .paidSickLeave(0)
                                .build()
                );

        MonthlyAbsencesDto actual = workerResource.getAllAbsencesForMonthAndEmployee(userAsEmployee.getUserId(), YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth()));


        assertThat(actual.getAvailableVacationDays()).isEqualTo(availableVacationDays);
        assertThat(actual.getDoctorsVisitingTime()).isEqualTo(doctorsVisitingTime);
        assertThat(actual.getVacationDays()).isZero();
        assertThat(actual.getCompensatoryDays()).isZero();
        assertThat(actual.getNursingDays()).isZero();
        assertThat(actual.getMaternityLeaveDays()).isZero();
        assertThat(actual.getExternalTrainingDays()).isZero();
        assertThat(actual.getConferenceDays()).isZero();
        assertThat(actual.getMaternityProtectionDays()).isZero();
        assertThat(actual.getFatherMonthDays()).isZero();
        assertThat(actual.getPaidSpecialLeaveDays()).isZero();
        assertThat(actual.getNonPaidVacationDays()).isZero();
        assertThat(actual.getPaidSickLeave()).isZero();
    }

    @Test
    void getOfficeDaysForMonthAndEmployee_whenEmployeeHasAbsences_thenReturnAbsenceObjectWithValues() {
        User userForRole = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(userForRole);
        final Employee userAsEmployee = createEmployeeForUser(userForRole);

        LocalDate fromDate = DateUtils.getFirstDayOfCurrentMonth(LocalDate.now().toString());
        String toDateString = DateUtils.getLastDayOfCurrentMonth(fromDate);

        when(employeeService.getEmployee(anyString()))
                .thenReturn(userAsEmployee);

        when(workingTimeUtil.getAbsenceTimesForEmployee(any(), any(), any(LocalDate.class)))
                .thenReturn(4);

        when(dateHelperService.getNumberOfWorkingDaysForMonthWithoutHolidays(any(LocalDate.class)))
                .thenReturn(21);

        when(dateHelperService.getNumberOfFridaysInMonth(any(LocalDate.class)))
                .thenReturn(4); //mostly the case in reality

        when(absenceService.getNumberOfDaysAbsent(any(), any(LocalDate.class)))
                .thenReturn(7);

        when(absenceService.numberOfFridaysAbsent(any()))
                .thenReturn(1);

        when(dateHelperService.getCorrectDateForRequest(any(Employee.class), any(YearMonth.class)))
                .thenReturn(Pair.of(fromDate.toString(), toDateString));

        MonthlyOfficeDaysDto actual = workerResource.getOfficeDaysForMonthAndEmployee(userAsEmployee.getUserId(), YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth()));

        assertThat(actual.getHomeofficeDays()).isEqualTo(4);
        assertThat(actual.getOfficeDays()).isEqualTo(14);
        assertThat(actual.getFridaysAtTheOffice()).isEqualTo(3);
    }

    @Test
    void getOfficeDaysForMonthAndEmployee_whenEmployeeHasNoAbsences_thenReturnAbsenceObjectWithZeroHomeOfficeDaysAndAllFridaysAtOffice() {
        User userForRole = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(userForRole);
        final Employee userAsEmployee = createEmployeeForUser(userForRole);

        LocalDate fromDate = DateUtils.getFirstDayOfCurrentMonth(LocalDate.now().toString());
        String toDateString = DateUtils.getLastDayOfCurrentMonth(fromDate);

        when(employeeService.getEmployee(anyString()))
                .thenReturn(userAsEmployee);

        when(workingTimeUtil.getAbsenceTimesForEmployee(any(), any(), any(LocalDate.class)))
                .thenReturn(0);

        when(dateHelperService.getNumberOfWorkingDaysForMonthWithoutHolidays(any(LocalDate.class)))
                .thenReturn(21);

        when(dateHelperService.getNumberOfFridaysInMonth(any(LocalDate.class)))
                .thenReturn(4); //mostly the case in reality

        when(absenceService.getNumberOfDaysAbsent(any(), any(LocalDate.class)))
                .thenReturn(0);

        when(absenceService.numberOfFridaysAbsent(any()))
                .thenReturn(0);

        when(dateHelperService.getCorrectDateForRequest(any(Employee.class), any(YearMonth.class)))
                .thenReturn(Pair.of(fromDate.toString(), toDateString));

        MonthlyOfficeDaysDto actual = workerResource.getOfficeDaysForMonthAndEmployee(userAsEmployee.getUserId(), YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth()));

        assertThat(actual.getHomeofficeDays()).isZero();
        assertThat(actual.getOfficeDays()).isEqualTo(21);
        assertThat(actual.getFridaysAtTheOffice()).isEqualTo(4);
    }

    @Test
    void getAllWarningsForEmployeeAndMonth_whenHasWarnings_thenReturnListOfMonthlyWarningDtos() {
        User userForRole = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(userForRole);
        final Employee userAsEmployee = createEmployeeForUser(userForRole);
        LocalDate fromDate = DateUtils.getFirstDayOfCurrentMonth(LocalDate.now().toString());
        String toDateString = DateUtils.getLastDayOfCurrentMonth(fromDate);
        Pair<String, String> datePair = Pair.of(fromDate.toString(), toDateString);

        when(employeeService.getEmployee(anyString()))
                .thenReturn(userAsEmployee);

        when(dateHelperService.getCorrectDateForRequest(any(Employee.class), any(YearMonth.class)))
                .thenReturn(datePair);

        when(zepService.getAbsenceForEmployee(any(Employee.class), any(LocalDate.class)))
                .thenReturn(createAbsenceTimeListForRequest(userAsEmployee.getUserId()));

        when(zepService.getProjectTimes(any(Employee.class), any(LocalDate.class)))
                .thenReturn(createProjectEntryListForRequest());

        when(timeWarningService.getAllTimeWarningsForEmployeeAndMonth(any(), any(), any(Employee.class)))
                .thenReturn(createMonthlyWarningListForRequest());

        List<MonthlyWarningDto> mappedWarnings = createMonthlyWarningListForRequest().stream()
                .map(monthlyWarningMapper::mapToDto)
                .toList();

        List<MonthlyWarningDto> actual = workerResource.getAllWarningsForEmployeeAndMonth(userAsEmployee.getUserId(), YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth()));

        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.size()).isEqualTo(mappedWarnings.size());
        assertThat(actual.get(0).getName()).isEqualTo(mappedWarnings.get(0).getName());
    }

    @Test
    void getAllWarningsForEmployeeAndMonth_whenHasNoWarnings_thenReturnEmptyList() {
        User userForRole = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(userForRole);
        final Employee userAsEmployee = createEmployeeForUser(userForRole);
        LocalDate fromDate = DateUtils.getFirstDayOfCurrentMonth(LocalDate.now().toString());
        String toDateString = DateUtils.getLastDayOfCurrentMonth(fromDate);
        Pair<String, String> datePair = Pair.of(fromDate.toString(), toDateString);

        when(employeeService.getEmployee(anyString()))
                .thenReturn(userAsEmployee);

        when(dateHelperService.getCorrectDateForRequest(any(Employee.class), any(YearMonth.class)))
                .thenReturn(datePair);

        when(zepService.getAbsenceForEmployee(any(Employee.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        when(zepService.getProjectTimes(any(Employee.class), any(LocalDate.class)))
                .thenReturn(createProjectEntryListForMonth());

        when(timeWarningService.getAllTimeWarningsForEmployeeAndMonth(any(), any(), any(Employee.class)))
                .thenReturn(new ArrayList<>());


        List<MonthlyWarningDto> actual = workerResource.getAllWarningsForEmployeeAndMonth(userAsEmployee.getUserId(), YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth()));

        assertThat(actual.isEmpty()).isTrue();
    }


    private List<ProjectEntry> createProjectEntryListForRequest() {
        List<ProjectEntry> projectEntries = new ArrayList<>();
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 1, 15, 0),
                LocalDateTime.of(2024, 5, 1, 0, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 3, 12, 45),
                LocalDateTime.of(2024, 5, 3, 23, 45)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 18, 14, 45),
                LocalDateTime.of(2024, 5, 18, 15, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 21, 16, 0),
                LocalDateTime.of(2024, 5, 21, 21, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 22, 5, 0),
                LocalDateTime.of(2024, 5, 22, 8, 30)));
        projectEntries.add(createJourneyTimeEntry(LocalDateTime.of(2024, 5, 23, 15, 30),
                LocalDateTime.of(2024, 5, 23, 16, 15)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 23, 16, 30),
                LocalDateTime.of(2024, 5, 23, 17, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 27, 8, 0),
                LocalDateTime.of(2024, 5, 27, 14, 45)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 28, 8, 0),
                LocalDateTime.of(2024, 5, 28, 12, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 28, 12, 30),
                LocalDateTime.of(2024, 5, 28, 14, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 29, 5, 30),
                LocalDateTime.of(2024, 5, 29, 11, 0)));

        return projectEntries;
    }

    private List<ProjectEntry> createProjectEntryListForMonth() {
        List<ProjectEntry> projectEntries = new ArrayList<>();
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 3, 7, 0),
                LocalDateTime.of(2024, 6, 3, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 3, 12, 0),
                LocalDateTime.of(2024, 6, 3, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 4, 7, 0),
                LocalDateTime.of(2024, 6, 4, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 4, 12, 0),
                LocalDateTime.of(2024, 6, 4, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 5, 7, 0),
                LocalDateTime.of(2024, 6, 5, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 5, 12, 0),
                LocalDateTime.of(2024, 6, 5, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 6, 7, 0),
                LocalDateTime.of(2024, 6, 6, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 6, 12, 0),
                LocalDateTime.of(2024, 6, 6, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 7, 7, 0),
                LocalDateTime.of(2024, 6, 7, 12, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 7, 12, 30),
                LocalDateTime.of(2024, 6, 7, 14, 0)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 10, 7, 0),
                LocalDateTime.of(2024, 6, 10, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 10, 12, 0),
                LocalDateTime.of(2024, 6, 10, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 11, 7, 0),
                LocalDateTime.of(2024, 6, 11, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 11, 12, 0),
                LocalDateTime.of(2024, 6, 11, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 12, 7, 0),
                LocalDateTime.of(2024, 6, 12, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 12, 12, 0),
                LocalDateTime.of(2024, 6, 12, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 13, 7, 0),
                LocalDateTime.of(2024, 6, 13, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 13, 12, 0),
                LocalDateTime.of(2024, 6, 13, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 14, 7, 0),
                LocalDateTime.of(2024, 6, 14, 12, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 14, 12, 30),
                LocalDateTime.of(2024, 6, 14, 14, 0)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 17, 7, 0),
                LocalDateTime.of(2024, 6, 17, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 17, 12, 0),
                LocalDateTime.of(2024, 6, 17, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 18, 7, 0),
                LocalDateTime.of(2024, 6, 18, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 18, 12, 0),
                LocalDateTime.of(2024, 6, 18, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 19, 7, 0),
                LocalDateTime.of(2024, 6, 19, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 19, 12, 0),
                LocalDateTime.of(2024, 6, 19, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 20, 7, 0),
                LocalDateTime.of(2024, 6, 20, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 20, 12, 0),
                LocalDateTime.of(2024, 6, 20, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 21, 7, 0),
                LocalDateTime.of(2024, 6, 21, 12, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 21, 12, 30),
                LocalDateTime.of(2024, 6, 21, 14, 0)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 24, 7, 0),
                LocalDateTime.of(2024, 6, 24, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 24, 12, 0),
                LocalDateTime.of(2024, 6, 24, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 25, 7, 0),
                LocalDateTime.of(2024, 6, 25, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 25, 12, 0),
                LocalDateTime.of(2024, 6, 25, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 26, 7, 0),
                LocalDateTime.of(2024, 6, 26, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 26, 12, 0),
                LocalDateTime.of(2024, 6, 26, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 27, 7, 0),
                LocalDateTime.of(2024, 6, 27, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 27, 12, 0),
                LocalDateTime.of(2024, 6, 27, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 28, 7, 0),
                LocalDateTime.of(2024, 6, 28, 12, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 28, 12, 30),
                LocalDateTime.of(2024, 6, 28, 14, 0)));

        return projectEntries;
    }

    private List<MonthlyWarning> createMonthlyWarningListForRequest() {
        return List.of(
                createMonthlyWarning("Zeit-Buchung außerhalb der Kernarbeitszeit", List.of("2024-05-03", "2024-05-22", "2024-05-29")),
                createMonthlyWarning("Keine Zeit-Buchung vorhanden",
                        List.of("2024-05-06", "2024-05-07", "2024-05-08", "2024-05-10", "2024-05-13", "2024-05-14","2024-05-15", "2024-05-16", "2024-05-17," +
                                "2024-05-24", "2024-05-31")),
                createMonthlyWarning("Zeit-Buchung an einem Feiertag", List.of("2024-05-01")),
                createMonthlyWarning("Zeit-Buchung am Wochenende", List.of("2024-05-18")),
                createMonthlyWarning("Zu wenig Pausenzeit eingetragen", List.of("2024-05-03", "2024-05-27")),
                createMonthlyWarning("Ruhezeit wurde nicht eingehalten", List.of("2024-05-22", "2024-05-29")),
                createMonthlyWarning("Zu viel Arbeitszeit gebucht", List.of("2024-05-03", "2024-05-28")),
                createMonthlyWarning("Rückreise fehlt oder ist nach dem Zeitraum", List.of("2024-05-23")),
                createMonthlyWarning("Ungültiger Arbeitsort während einer Reise", List.of("2024-05-23"))
        );
    }

    private MonthlyWarning createMonthlyWarning(String name, List<String> dates) {
        return MonthlyWarning.builder()
                .name(name)
                .datesWhenWarningOccurred(new ArrayList<>(dates))
                .build();
    }

    private ProjectEntry createProjectTimeEntry(LocalDateTime from, LocalDateTime to) {
        return ProjectTimeEntry.builder()
                .fromTime(from)
                .toTime(to)
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
                .process("1033")
                .build();
    }

    private JourneyTimeEntry createJourneyTimeEntry(LocalDateTime from, LocalDateTime to) {
        return JourneyTimeEntry.builder()
                .fromTime(from)
                .toTime(to)
                .task(Task.REISEN)
                .workingLocation(WorkingLocation.A)
                .journeyDirection(JourneyDirection.TO)
                .vehicle(Vehicle.OTHER_INACTIVE)
                .build();
    }


    private List<AbsenceTime> createAbsenceTimeListForRequest(String userId) {
        List<AbsenceTime> absenceTimes = new ArrayList<>();
        absenceTimes.add(createAbsenceTime(userId, "2024-05-02", "2024-05-02", "KR"));
        absenceTimes.add(createAbsenceTime(userId, "2024-05-31", "2024-05-31", "HO"));
        return absenceTimes;
    }

    private AbsenceTime createAbsenceTime(String userId, String fromDate, String toDate, String reason) {
        return AbsenceTime.builder()
                .userId(userId)
                .fromDate(DateUtils.parseDate(fromDate))
                .toDate(DateUtils.parseDate(toDate))
                .reason(reason)
                .accepted(true)
                .build();
    }

    private List<AbsenceTime> createAbsenceListForEmployee() {
        return List.of(
                createAbsenceTime(LocalDate.now(), LocalDate.now(), AbsenceType.CONFERENCE_DAYS.getAbsenceName()),
                createAbsenceTime(LocalDate.now(), LocalDate.now(), AbsenceType.VACATION_DAYS.getAbsenceName()),
                createAbsenceTime(LocalDate.now(), LocalDate.now(), AbsenceType.MATERNITY_LEAVE_DAYS.getAbsenceName()),
                createAbsenceTime(LocalDate.now(), LocalDate.now(), AbsenceType.PAID_SICK_LEAVE.getAbsenceName())
        );
    }


    private MonthlyBillInfo createMonthlyBillInfo(int sumBills, int sumPrivateBills, int sumCompanyBills, boolean hasAttachmentWarnings, boolean hasCreditCard) {
        return MonthlyBillInfo.builder()
                .sumBills(sumBills)
                .sumPrivateBills(sumPrivateBills)
                .sumCompanyBills(sumCompanyBills)
                .hasAttachmentWarnings(hasAttachmentWarnings)
                .employeeHasCreditCard(hasCreditCard)
                .build();
    }

    private MonthlyBillInfoDto createMonthlyBillInfoDto(int sumBills, int sumPrivateBills, int sumCompanyBills, boolean hasAttachmentWarnings, boolean hasCreditCard) {
        return MonthlyBillInfoDto.builder()
                .sumBills(sumBills)
                .sumPrivateBills(sumPrivateBills)
                .sumCompanyBills(sumCompanyBills)
                .hasAttachmentWarnings(hasAttachmentWarnings)
                .employeeHasCreditCard(hasCreditCard)
                .build();
    }


    private List<AbsenceTime> createAbsenceListForEmployeeWithNoAbsences() {
        return List.of();
    }

    private AbsenceTime createAbsenceTime(LocalDate startDate, LocalDate endDate, String reason) {
        return AbsenceTime.builder()
                    .fromDate(startDate)
                    .toDate(endDate)
                    .reason(reason)
                    .accepted(true)
                    .build();
    }

    private ProjectHoursSummary createProjectHourSummary(String projectName, double billableHoursSum, double nonBillableHours) {
        return ProjectHoursSummary.builder()
                                  .projectName(projectName)
                                  .billableHoursSum(billableHoursSum)
                                  .nonBillableHoursSum(nonBillableHours)
                                  .chargeability(25.0)
                                  .isInternalProject(true)
                                  .build();
    }

    private List<ProjectHoursSummary> getProjectsHoursSummaryForEmployee() {
        return List.of(
            createProjectHourSummary("Testproject", 45.5, 21.5),
            createProjectHourSummary("Testproject2", 49.5, 20.5),
            createProjectHourSummary("Testproject3", 55.5, 20.5),
            createProjectHourSummary("Testproject4", 70.0, 20.5)
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
