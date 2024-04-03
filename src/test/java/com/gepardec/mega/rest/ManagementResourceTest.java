package com.gepardec.mega.rest;

import com.gepardec.mega.db.entity.common.State;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.Step;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.db.entity.project.ProjectEntry;
import com.gepardec.mega.db.entity.project.ProjectStep;
import com.gepardec.mega.domain.model.*;
import com.gepardec.mega.rest.model.ManagementEntryDto;
import com.gepardec.mega.rest.model.ProjectManagementEntryDto;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.ProjectEntryService;
import com.gepardec.mega.service.api.StepEntryService;
import com.gepardec.mega.service.helper.WorkingTimeUtil;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.impl.Rest;
import com.gepardec.mega.zep.impl.Soap;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
@JwtSecurity(claims = {
        @Claim(key = "email", value = "test@gepardec.com")
})
class ManagementResourceTest {

    @InjectMock
    EmployeeService employeeService;

    @InjectMock
    StepEntryService stepEntryService;

    @InjectMock
    CommentService commentService;

    @InjectMock
    ProjectEntryService projectEntryService;

    @InjectMock
    ZepService zepService;

    @InjectMock
    WorkingTimeUtil workingTimeUtil;

    @InjectMock
    UserContext userContext;

    @Test
    @TestSecurity
    @JwtSecurity
    void getAllOfficeManagementEntries_whenNotLogged_thenReturnsHttpStatusUNAUTHORIZED() {
        when(userContext.getUser()).thenReturn(createUserForRole(Role.OFFICE_MANAGEMENT));
        given().contentType(ContentType.JSON)
                .get("/management/officemanagemententries/2020/11")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void getAllOfficeManagementEntries_whenPOST_thenReturnsHttpStatusMETHOD_NOT_ALLOWED() {
        given().contentType(ContentType.JSON)
                .post("/management/officemanagemententries/2020/11")
                .then().assertThat().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void getAllOfficeManagementEntries_whenValid_thenReturnsListOfEntries() {
        final User user = createUserForRole(Role.OFFICE_MANAGEMENT);
        when(userContext.getUser()).thenReturn(user);

        when(employeeService.getAllEmployeesConsideringExitDate(any()))
                .thenReturn(List.of(Employee.builder().releaseDate("2020-01-01").email("no-reply@gepardec.com").build()));

        List<StepEntry> entries = List.of(
                createStepEntryForStep(StepName.CONTROL_INTERNAL_TIMES, EmployeeState.OPEN),
                createStepEntryForStep(StepName.CONTROL_TIME_EVIDENCES, EmployeeState.DONE),
                createStepEntryForStep(StepName.CONTROL_TIMES, EmployeeState.OPEN)
        );

        when(commentService.countFinishedAndTotalComments(
                anyString(), any(LocalDate.class), any(LocalDate.class))
        ).thenReturn(FinishedAndTotalComments.builder().finishedComments(2L).totalComments(3L).build());

        when(stepEntryService.findAllStepEntriesForEmployee(
                any(Employee.class), any(LocalDate.class), any(LocalDate.class))
        ).thenReturn(entries);

        when(zepService.getProjectTimesForEmployeePerProject(
                ArgumentMatchers.anyString(), any(LocalDate.class)
        )).thenReturn(Collections.emptyList());

        List<ManagementEntryDto> result = given().contentType(ContentType.JSON)
                .get("/management/officemanagemententries/2020/01")
                .as(new TypeRef<>() {
                });

        assertThat(result).hasSize(1);
        ManagementEntryDto entry = result.get(0);
        assertThat(entry.getInternalCheckState()).isEqualTo(com.gepardec.mega.domain.model.State.OPEN);
        assertThat(entry.getEmployeeCheckState()).isEqualTo(com.gepardec.mega.domain.model.State.OPEN);
        assertThat(entry.getProjectCheckState()).isEqualTo(com.gepardec.mega.domain.model.State.DONE);
        assertThat(entry.getEmployee().getEmail()).isEqualTo("no-reply@gepardec.com");
        assertThat(entry.getEmployee().getReleaseDate()).isEqualTo("2020-01-01");
        assertThat(entry.getTotalComments()).isEqualTo(3L);
        assertThat(entry.getFinishedComments()).isEqualTo(2L);
    }

    @Test
    void getAllOfficeManagementEntries_whenNoActiveEmployeesFound_thenReturnsEmptyResultList() {
        final User user = createUserForRole(Role.OFFICE_MANAGEMENT);
        when(userContext.getUser()).thenReturn(user);

        when(employeeService.getAllEmployeesConsideringExitDate(any())).thenReturn(List.of());

        List<StepEntry> entries = List.of(
                createStepEntryForStep(StepName.CONTROL_INTERNAL_TIMES, EmployeeState.OPEN),
                createStepEntryForStep(StepName.CONTROL_TIME_EVIDENCES, EmployeeState.DONE),
                createStepEntryForStep(StepName.CONTROL_TIMES, EmployeeState.OPEN)
        );

        when(commentService.countFinishedAndTotalComments(
                anyString(), any(LocalDate.class), any(LocalDate.class))
        ).thenReturn(FinishedAndTotalComments.builder().finishedComments(2L).totalComments(3L).build());

        when(stepEntryService.findAllStepEntriesForEmployee(any(Employee.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(entries);

        List<ManagementEntryDto> result = given().contentType(ContentType.JSON)
                .get("/management/officemanagemententries/2020/10")
                .as(new TypeRef<>() {
                });

        assertThat(result).isEmpty();
    }

    @Test
    void getAllOfficeManagementEntries_whenNoStepEntriesFound_thenReturnsEmptyResultList() {
        final User user = createUserForRole(Role.OFFICE_MANAGEMENT);
        when(userContext.getUser()).thenReturn(user);

        when(commentService.countFinishedAndTotalComments(
                anyString(), any(LocalDate.class), any(LocalDate.class))
        ).thenReturn(FinishedAndTotalComments.builder().finishedComments(2L).totalComments(3L).build());

        when(stepEntryService.findAllStepEntriesForEmployee(any(Employee.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        when(employeeService.getAllEmployeesConsideringExitDate(any()))
                .thenReturn(List.of());

        List<ManagementEntryDto> result = given().contentType(ContentType.JSON)
                .get("/management/officemanagemententries/2020/11")
                .as(new TypeRef<>() {
                });

        assertThat(result).isEmpty();
    }

    @Test
    @TestSecurity
    @JwtSecurity
    void getAllProjectManagementEntries_whenNotLogged_thenReturnsHttpStatusUNAUTHORIZED() {
        when(userContext.getUser()).thenReturn(createUserForRole(Role.PROJECT_LEAD));
        given().contentType(ContentType.JSON)
                .get("/management/projectmanagemententries/2020/11")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void getAllProjectManagementEntries_whenPOST_thenReturnsStatusMETHOD_NOT_ALLOWED() {
        given().contentType(ContentType.JSON)
                .post("/management/projectmanagemententries/2020/11")
                .then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void getAllProjectManagementEntries_whenValid_thenReturnsListOfEntries() {
        final User user = createUserForRole(Role.PROJECT_LEAD);
        when(userContext.getUser()).thenReturn(user);

        Employee employee1 = createEmployee("008", "no-reply@gepardec.com", "Max", "Mustermann");
        Employee employee2 = createEmployee("030", "no-reply@gepardec.com", "Max", "Mustermann");

        List<String> employees = List.of(employee1.getUserId(), employee2.getUserId());
        ProjectEmployees rgkkcc = createProject("ÖGK-RGKKCC-2020", employees);
        ProjectEmployees rgkkwc = createProject("ÖGK-RGKK2WC-2020", employees);
        when(stepEntryService.getProjectEmployeesForPM(any(LocalDate.class), any(LocalDate.class), ArgumentMatchers.anyString()))
                .thenReturn(List.of(rgkkcc, rgkkwc));

        when(employeeService.getAllEmployeesConsideringExitDate(any())).thenReturn(List.of(employee1, employee2));

        List<StepEntry> stepEntries = List.of(
                createStepEntryForStep(StepName.CONTROL_INTERNAL_TIMES, EmployeeState.OPEN),
                createStepEntryForStep(StepName.CONTROL_TIME_EVIDENCES, EmployeeState.DONE),
                createStepEntryForStep(StepName.CONTROL_TIMES, EmployeeState.OPEN)
        );

        List<ProjectEntry> projectEntries = List.of(
                createProjectEntryForStepWithStateAndPreset(ProjectStep.CONTROL_PROJECT, State.NOT_RELEVANT, true),
                createProjectEntryForStepWithStateAndPreset(ProjectStep.CONTROL_BILLING, State.DONE, false)
        );

        when(commentService.countFinishedAndTotalComments(
                anyString(), any(LocalDate.class), any(LocalDate.class))
        ).thenReturn(FinishedAndTotalComments.builder().finishedComments(2L).totalComments(3L).build());

        when(stepEntryService.findAllStepEntriesForEmployeeAndProject(
                any(Employee.class), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                any(LocalDate.class), any(LocalDate.class))
        ).thenReturn(stepEntries);

        when(projectEntryService.findByNameAndDate(ArgumentMatchers.anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(projectEntries);

        when(zepService.getProjectTimesForEmployeePerProject(
                ArgumentMatchers.anyString(), any(LocalDate.class)
        )).thenReturn(getProjectTimeTypeList());

        when(workingTimeUtil.getBillableTimesForEmployee(anyList(), any(Employee.class))).thenReturn("02:00");
        when(workingTimeUtil.getInternalTimesForEmployee(anyList(), any(Employee.class))).thenReturn("02:00");

        List<ProjectManagementEntryDto> result = given().contentType(ContentType.JSON)
                .get("/management/projectmanagemententries/2020/10")
                .as(new TypeRef<>() {
                });

        assertThat(result).hasSize(2);
        Optional<ProjectManagementEntryDto> projectRgkkcc = result.stream()
                .filter(p -> rgkkcc.getProjectId().equalsIgnoreCase(p.getProjectName()))
                .findFirst();

        // assert project management entry
        assertThat(projectRgkkcc).isPresent();
        assertThat(projectRgkkcc.get().getControlProjectState()).isEqualTo(com.gepardec.mega.domain.model.ProjectState.NOT_RELEVANT);
        assertThat(projectRgkkcc.get().getControlBillingState()).isEqualTo(com.gepardec.mega.domain.model.ProjectState.DONE);
        assertThat(projectRgkkcc.get().getPresetControlProjectState()).isTrue();
        assertThat(projectRgkkcc.get().getPresetControlBillingState()).isFalse();

        List<ManagementEntryDto> rgkkccEntries = projectRgkkcc.get().getEntries();
        Optional<ManagementEntryDto> entrymmustermann = rgkkccEntries.stream()
                .filter(m -> employee1.getUserId().equalsIgnoreCase(m.getEmployee().getUserId()))
                .findFirst();
        // assert management entry
        assertThat(entrymmustermann).isPresent();
        ManagementEntryDto entry = entrymmustermann.get();
        assertThat(entry.getInternalCheckState()).isEqualTo(com.gepardec.mega.domain.model.State.OPEN);
        assertThat(entry.getEmployeeCheckState()).isEqualTo(com.gepardec.mega.domain.model.State.OPEN);
        assertThat(entry.getProjectCheckState()).isEqualTo(com.gepardec.mega.domain.model.State.DONE);
        assertThat(entry.getEmployee().getEmail()).isEqualTo(employee1.getEmail());
        assertThat(entry.getEmployee().getReleaseDate()).isEqualTo(employee1.getReleaseDate());
        assertThat(entry.getTotalComments()).isEqualTo(3L);
        assertThat(entry.getFinishedComments()).isEqualTo(2L);
    }

    @Test
    void getProjectManagementEntries_whenProjectTimes_thenCorrectAggregatedWorkTimes() {
        final User user = createUserForRole(Role.PROJECT_LEAD);
        when(userContext.getUser()).thenReturn(user);

        Employee employee1 = createEmployee("008", "no-reply@gepardec.com", "Max", "Mustermann");
        Employee employee2 = createEmployee("030", "no-reply@gepardec.com", "Max", "Mustermann");

        List<String> employees = List.of(employee1.getUserId(), employee2.getUserId());
        ProjectEmployees rgkkcc = createProject("ÖGK-RGKKCC-2020", employees);
        ProjectEmployees rgkkwc = createProject("ÖGK-RGKK2WC-2020", employees);
        when(stepEntryService.getProjectEmployeesForPM(any(LocalDate.class), any(LocalDate.class), ArgumentMatchers.anyString()))
                .thenReturn(List.of(rgkkcc, rgkkwc));

        when(employeeService.getAllEmployeesConsideringExitDate(any())).thenReturn(List.of(employee1, employee2));

        List<StepEntry> stepEntries = List.of(
                createStepEntryForStep(StepName.CONTROL_INTERNAL_TIMES, EmployeeState.OPEN),
                createStepEntryForStep(StepName.CONTROL_TIME_EVIDENCES, EmployeeState.DONE),
                createStepEntryForStep(StepName.CONTROL_TIMES, EmployeeState.OPEN)
        );

        List<ProjectEntry> projectEntries = List.of(
                createProjectEntryForStepWithStateAndPreset(ProjectStep.CONTROL_PROJECT, State.NOT_RELEVANT, true),
                createProjectEntryForStepWithStateAndPreset(ProjectStep.CONTROL_BILLING, State.DONE, false)
        );

        when(commentService.countFinishedAndTotalComments(
                anyString(), any(LocalDate.class), any(LocalDate.class))
        ).thenReturn(FinishedAndTotalComments.builder().finishedComments(2L).totalComments(3L).build());

        when(stepEntryService.findAllStepEntriesForEmployeeAndProject(
                any(Employee.class), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                any(LocalDate.class), any(LocalDate.class))
        ).thenReturn(stepEntries);

        when(projectEntryService.findByNameAndDate(ArgumentMatchers.anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(projectEntries);

        when(zepService.getProjectTimesForEmployeePerProject(
                ArgumentMatchers.anyString(), any(LocalDate.class)
        )).thenReturn(getProjectTimeTypeList());

        when(workingTimeUtil.getInternalTimesForEmployee(anyList(), any(Employee.class))).thenReturn("01:00");
        when(workingTimeUtil.getBillableTimesForEmployee(anyList(), any(Employee.class))).thenReturn("02:00");

        List<ProjectManagementEntryDto> result = given().contentType(ContentType.JSON)
                .get("/management/projectmanagemententries/2020/09")
                .as(new TypeRef<>() {
                });

        assertThat(result).hasSize(2);
        Optional<ProjectManagementEntryDto> projectRgkkcc = result.stream()
                .filter(p -> rgkkcc.getProjectId().equalsIgnoreCase(p.getProjectName()))
                .findFirst();

        // assert project management entry
        assertThat(projectRgkkcc).isPresent();
        assertThat(projectRgkkcc.get().getControlProjectState()).isEqualTo(com.gepardec.mega.domain.model.ProjectState.NOT_RELEVANT);
        assertThat(projectRgkkcc.get().getControlBillingState()).isEqualTo(com.gepardec.mega.domain.model.ProjectState.DONE);
        assertThat(projectRgkkcc.get().getPresetControlProjectState()).isTrue();
        assertThat(projectRgkkcc.get().getPresetControlBillingState()).isFalse();

        List<ManagementEntryDto> rgkkccEntries = projectRgkkcc.get().getEntries();
        Optional<ManagementEntryDto> entrymmustermann = rgkkccEntries.stream()
                .filter(m -> employee1.getUserId().equalsIgnoreCase(m.getEmployee().getUserId()))
                .findFirst();
        // assert management entry
        assertThat(entrymmustermann).isPresent();
        ManagementEntryDto entry = entrymmustermann.get();
        assertThat(entry.getInternalCheckState()).isEqualTo(com.gepardec.mega.domain.model.State.OPEN);
        assertThat(entry.getEmployeeCheckState()).isEqualTo(com.gepardec.mega.domain.model.State.OPEN);
        assertThat(entry.getProjectCheckState()).isEqualTo(com.gepardec.mega.domain.model.State.DONE);
        assertThat(entry.getEmployee().getEmail()).isEqualTo(employee1.getEmail());
        assertThat(entry.getEmployee().getReleaseDate()).isEqualTo(employee1.getReleaseDate());
        assertThat(entry.getTotalComments()).isEqualTo(3L);
        assertThat(entry.getFinishedComments()).isEqualTo(2L);

        // assert billable/non billable time
        assertThat(result.get(0).getAggregatedBillableWorkTimeInSeconds()).isEqualTo(Duration.ofMinutes(240));
        assertThat(result.get(0).getAggregatedNonBillableWorkTimeInSeconds()).isEqualTo(Duration.ofMinutes(120));
    }

    @Test
    void getProjectManagementEntries_whenNoProjectTimes_thenZeroAggregatedWorkTimes() {
        final User user = createUserForRole(Role.PROJECT_LEAD);
        when(userContext.getUser()).thenReturn(user);

        Employee employee1 = createEmployee("008", "no-reply@gepardec.com", "Max", "Mustermann");
        Employee employee2 = createEmployee("030", "no-reply@gepardec.com", "Max", "Mustermann");

        List<String> employees = List.of(employee1.getUserId(), employee2.getUserId());
        ProjectEmployees rgkkcc = createProject("ÖGK-RGKKCC-2020", employees);
        ProjectEmployees rgkkwc = createProject("ÖGK-RGKK2WC-2020", employees);
        when(stepEntryService.getProjectEmployeesForPM(any(LocalDate.class), any(LocalDate.class), ArgumentMatchers.anyString()))
                .thenReturn(List.of(rgkkcc, rgkkwc));

        when(employeeService.getAllEmployeesConsideringExitDate(any())).thenReturn(List.of(employee1, employee2));

        List<StepEntry> stepEntries = List.of(
                createStepEntryForStep(StepName.CONTROL_INTERNAL_TIMES, EmployeeState.OPEN),
                createStepEntryForStep(StepName.CONTROL_TIME_EVIDENCES, EmployeeState.DONE),
                createStepEntryForStep(StepName.CONTROL_TIMES, EmployeeState.OPEN)
        );

        List<ProjectEntry> projectEntries = List.of(
                createProjectEntryForStepWithStateAndPreset(ProjectStep.CONTROL_PROJECT, State.NOT_RELEVANT, true),
                createProjectEntryForStepWithStateAndPreset(ProjectStep.CONTROL_BILLING, State.DONE, false)
        );

        when(commentService.countFinishedAndTotalComments(
                anyString(), any(LocalDate.class), any(LocalDate.class))
        ).thenReturn(FinishedAndTotalComments.builder().finishedComments(2L).totalComments(3L).build());

        when(stepEntryService.findAllStepEntriesForEmployeeAndProject(
                any(Employee.class), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                any(LocalDate.class), any(LocalDate.class))
        ).thenReturn(stepEntries);

        when(projectEntryService.findByNameAndDate(ArgumentMatchers.anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(projectEntries);

        when(zepService.getProjectTimesForEmployeePerProject(
                ArgumentMatchers.anyString(), any(LocalDate.class)
        )).thenReturn(getProjectTimeTypeList());

        when(workingTimeUtil.getBillableTimesForEmployee(anyList(), any(Employee.class))).thenReturn("00:00");
        when(workingTimeUtil.getInternalTimesForEmployee(anyList(), any(Employee.class))).thenReturn("00:00");

        List<ProjectManagementEntryDto> result = given().contentType(ContentType.JSON)
                .get("/management/projectmanagemententries/2020/09")
                .as(new TypeRef<>() {
                });

        assertThat(result).hasSize(2);
        Optional<ProjectManagementEntryDto> projectRgkkcc = result.stream()
                .filter(p -> rgkkcc.getProjectId().equalsIgnoreCase(p.getProjectName()))
                .findFirst();

        // assert project management entry
        assertThat(projectRgkkcc).isPresent();
        assertThat(projectRgkkcc.get().getControlProjectState()).isEqualTo(com.gepardec.mega.domain.model.ProjectState.NOT_RELEVANT);
        assertThat(projectRgkkcc.get().getControlBillingState()).isEqualTo(com.gepardec.mega.domain.model.ProjectState.DONE);
        assertThat(projectRgkkcc.get().getPresetControlProjectState()).isTrue();
        assertThat(projectRgkkcc.get().getPresetControlBillingState()).isFalse();

        List<ManagementEntryDto> rgkkccEntries = projectRgkkcc.get().getEntries();
        Optional<ManagementEntryDto> entrymmustermann = rgkkccEntries.stream()
                .filter(m -> employee1.getUserId().equalsIgnoreCase(m.getEmployee().getUserId()))
                .findFirst();
        // assert management entry
        assertThat(entrymmustermann).isPresent();
        ManagementEntryDto entry = entrymmustermann.get();
        assertThat(entry.getInternalCheckState()).isEqualTo(com.gepardec.mega.domain.model.State.OPEN);
        assertThat(entry.getEmployeeCheckState()).isEqualTo(com.gepardec.mega.domain.model.State.OPEN);
        assertThat(entry.getProjectCheckState()).isEqualTo(com.gepardec.mega.domain.model.State.DONE);
        assertThat(entry.getEmployee().getEmail()).isEqualTo(employee1.getEmail());
        assertThat(entry.getEmployee().getReleaseDate()).isEqualTo(employee1.getReleaseDate());
        assertThat(entry.getTotalComments()).isEqualTo(3L);
        assertThat(entry.getFinishedComments()).isEqualTo(2L);

        // assert billable/non billable time
        assertThat(result.get(0).getAggregatedBillableWorkTimeInSeconds()).isEqualTo(Duration.ofMinutes(0));
        assertThat(result.get(0).getAggregatedNonBillableWorkTimeInSeconds()).isEqualTo(Duration.ofMinutes(0));
    }

    @Test
    void getProjectManagementEntries_whenManagementEntryIsNull_thenNoNullPointerException() {
        final User user = createUserForRole(Role.PROJECT_LEAD);
        when(userContext.getUser()).thenReturn(user);

        Employee employee1 = createEmployee("008", "no-reply@gepardec.com", "Max", "Mustermann");
        Employee employee2 = createEmployee("030", "no-reply@gepardec.com", "Max", "Mustermann");

        List<String> employees = List.of(employee1.getUserId(), employee2.getUserId());
        ProjectEmployees rgkkcc = createProject("ÖGK-RGKKCC-2020", employees);
        ProjectEmployees rgkkwc = createProject("ÖGK-RGKK2WC-2020", employees);
        when(stepEntryService.getProjectEmployeesForPM(any(LocalDate.class), any(LocalDate.class), ArgumentMatchers.anyString()))
                .thenReturn(List.of(rgkkcc, rgkkwc));

        when(employeeService.getAllEmployeesConsideringExitDate(any())).thenReturn(List.of(employee1, employee2));

        List<StepEntry> stepEntries = List.of(
                createStepEntryForStep(StepName.CONTROL_INTERNAL_TIMES, EmployeeState.OPEN),
                createStepEntryForStep(StepName.CONTROL_TIME_EVIDENCES, EmployeeState.DONE),
                createStepEntryForStep(StepName.CONTROL_TIMES, EmployeeState.OPEN)
        );

        List<ProjectEntry> projectEntries = List.of(
                createProjectEntryForStepWithStateAndPreset(ProjectStep.CONTROL_PROJECT, State.NOT_RELEVANT, true),
                createProjectEntryForStepWithStateAndPreset(ProjectStep.CONTROL_BILLING, State.DONE, false)
        );

        when(commentService.countFinishedAndTotalComments(
                anyString(), any(LocalDate.class), any(LocalDate.class))
        ).thenReturn(FinishedAndTotalComments.builder().finishedComments(2L).totalComments(3L).build());

        when(stepEntryService.findAllStepEntriesForEmployeeAndProject(
                any(Employee.class), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                any(LocalDate.class), any(LocalDate.class))
        ).thenReturn(stepEntries);

        when(projectEntryService.findByNameAndDate(ArgumentMatchers.anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(projectEntries);

        when(zepService.getProjectTimesForEmployeePerProject(
                ArgumentMatchers.anyString(), any(LocalDate.class)
        )).thenReturn(getProjectTimeTypeList());

        List<ProjectManagementEntryDto> result = given().contentType(ContentType.JSON)
                .get("/management/projectmanagemententries/2020/09")
                .as(new TypeRef<>() {
                });

        // assert billable/non billable time
        assertThat(result.get(0).getAggregatedBillableWorkTimeInSeconds()).isEqualTo(Duration.ofMinutes(0));
        assertThat(result.get(0).getAggregatedNonBillableWorkTimeInSeconds()).isEqualTo(Duration.ofMinutes(0));
    }

    @Test
    void getAllProjectManagementEntries_whenNoProjectsFound_thenReturnsEmptyList() {
        final User user = createUserForRole(Role.PROJECT_LEAD);
        when(userContext.getUser()).thenReturn(user);

        List<ProjectManagementEntryDto> result = given().contentType(ContentType.JSON)
                .get("/management/projectmanagemententries/2020/11")
                .as(new TypeRef<>() {
                });

        assertThat(result).isEmpty();
    }

    @Test
    void getAllProjectManagementEntries_whenNoEmployeesAssignedToProject_thenReturnResultList() {
        final User user = createUserForRole(Role.PROJECT_LEAD);
        when(userContext.getUser()).thenReturn(user);

        ProjectEmployees rgkkcc = createProject("ÖGK-RGKKCC-2020", List.of());
        when(stepEntryService.getProjectEmployeesForPM(any(LocalDate.class), any(LocalDate.class), ArgumentMatchers.anyString()))
                .thenReturn(List.of(rgkkcc));

        List<ProjectManagementEntryDto> result = given().contentType(ContentType.JSON)
                .get("/management/projectmanagemententries/2020/11")
                .as(new TypeRef<>() {
                });

        assertThat(result).isEmpty();
    }

    @Test
    void getAllProjectManagementEntries_whenNoStepEntriesFound_thenReturnsResultList() {
        final User user = createUserForRole(Role.PROJECT_LEAD);
        when(userContext.getUser()).thenReturn(user);

        Employee employee1 = createEmployee("008", "no-reply@gepardec.com", "Max", "Mustermann");
        Employee employee2 = createEmployee("030", "no-reply@gepardec.com", "Max", "Mustermann");

        List<String> employees = List.of(employee1.getUserId(), employee2.getUserId());
        ProjectEmployees rgkkcc = createProject("ÖGK-RGKKCC-2020", employees);
        when(stepEntryService.getProjectEmployeesForPM(any(LocalDate.class), any(LocalDate.class), ArgumentMatchers.anyString()))
                .thenReturn(List.of(rgkkcc));

        when(stepEntryService.findAllStepEntriesForEmployee(any(Employee.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        List<ProjectManagementEntryDto> result = given().contentType(ContentType.JSON)
                .get("/management/projectmanagemententries/2020/11")
                .as(new TypeRef<>() {
                });

        assertThat(result).isEmpty();
    }

    private Step createStep(StepName stepName) {
        Step step = new Step();
        step.setName(stepName.name());
        return step;
    }

    private StepEntry createStepEntryForStep(StepName stepName, EmployeeState employeeState) {
        StepEntry stepEntry = new StepEntry();
        stepEntry.setStep(createStep(stepName));
        stepEntry.setState(employeeState);
        stepEntry.setDate(LocalDate.now());
        stepEntry.setAssignee(createUser());
        return stepEntry;
    }

    private com.gepardec.mega.db.entity.employee.User createUser() {
        com.gepardec.mega.db.entity.employee.User user = new com.gepardec.mega.db.entity.employee.User();
        user.setEmail("no-reply@gpeardec.com");
        return user;
    }

    private User createUserForRole(final Role role) {
        return User.builder()
                .dbId(1)
                .userId("005")
                .email("no-reply@gpeardec.com")
                .firstname("Max")
                .lastname("Mustermann")
                .roles(Set.of(role))
                .build();
    }

    private ProjectEmployees createProject(String projectId, List<String> employees) {
        return ProjectEmployees.builder()
                .projectId(projectId)
                .employees(employees)
                .build();
    }

    private Employee createEmployee(String userId, String email, String firstname, String lastname) {
        return Employee.builder()
                .userId(userId)
                .firstname(firstname)
                .lastname(lastname)
                .email(email)
                .build();
    }

    private ProjectEntry createProjectEntryForStepWithStateAndPreset(ProjectStep step, State state, boolean preset) {
        ProjectEntry p = new ProjectEntry();
        p.setStep(step);
        p.setState(state);
        p.setPreset(preset);
        return p;
    }

    private List<ProjectTime> getProjectTimeTypeList() {
        List<ProjectTime> timeType = new ArrayList<>();
        ProjectTime projektzeitType = ProjectTime.builder().build();
        projektzeitType.setDuration("4");
        projektzeitType.setBillable(true);

        ProjectTime projektzeitType1 = ProjectTime.builder().build();
        projektzeitType1.setDuration("0");

        ProjectTime projektzeitType2 = ProjectTime.builder().build();
        projektzeitType2.setDuration("2");
        projektzeitType2.setBillable(false);

        timeType.add(projektzeitType);
        timeType.add(projektzeitType1);
        timeType.add(projektzeitType2);

        return timeType;
    }
}
