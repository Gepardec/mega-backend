package com.gepardec.mega.rest.impl;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.Step;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.FinishedAndTotalComments;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.rest.model.CustomerProjectWithoutLeadsDto;
import com.gepardec.mega.rest.model.ManagementEntryDto;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.ProjectEntryService;
import com.gepardec.mega.service.api.ProjectService;
import com.gepardec.mega.service.api.StepEntryService;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.impl.Rest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
@OidcSecurity(claims = {
        @Claim(key = "email", value = "test@gepardec.com")
})
class OfficeManagementResourceImplTest {

    @InjectMock
    EmployeeService employeeService;

    @InjectMock
    StepEntryService stepEntryService;

    @InjectMock
    CommentService commentService;

    @InjectMock
    ProjectEntryService projectEntryService;

    @InjectMock
    @Rest
    ZepService zepService;

    @InjectMock
    UserContext userContext;

    @InjectMock
    ProjectService projectService;

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
                anyString(), any(YearMonth.class))
        ).thenReturn(FinishedAndTotalComments.builder().finishedComments(2L).totalComments(3L).build());

        when(stepEntryService.findAllStepEntriesForEmployee(
                any(Employee.class), any(YearMonth.class))
        ).thenReturn(entries);

        when(zepService.getProjectTimesForEmployeePerProject(
                anyString(), any(YearMonth.class)
        )).thenReturn(Collections.emptyList());

        List<ManagementEntryDto> result = given().contentType(ContentType.JSON)
                .get("/officemanagement/officemanagemententries/2020-01")
                .as(new TypeRef<>() {
                });

        assertThat(result).hasSize(1);
        ManagementEntryDto entry = result.getFirst();
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
                anyString(), any(YearMonth.class))
        ).thenReturn(FinishedAndTotalComments.builder().finishedComments(2L).totalComments(3L).build());

        when(stepEntryService.findAllStepEntriesForEmployee(any(Employee.class), any(YearMonth.class)))
                .thenReturn(entries);

        List<ManagementEntryDto> result = given().contentType(ContentType.JSON)
                .get("/officemanagement/officemanagemententries/2020-10")
                .as(new TypeRef<>() {
                });

        assertThat(result).isEmpty();
    }

    @Test
    void getAllOfficeManagementEntries_whenNoStepEntriesFound_thenReturnsEmptyResultList() {
        final User user = createUserForRole(Role.OFFICE_MANAGEMENT);
        when(userContext.getUser()).thenReturn(user);

        when(commentService.countFinishedAndTotalComments(
                anyString(), any(YearMonth.class))
        ).thenReturn(FinishedAndTotalComments.builder().finishedComments(2L).totalComments(3L).build());

        when(stepEntryService.findAllStepEntriesForEmployee(any(Employee.class), any(YearMonth.class)))
                .thenReturn(List.of());

        when(employeeService.getAllEmployeesConsideringExitDate(any()))
                .thenReturn(List.of());

        List<ManagementEntryDto> result = given().contentType(ContentType.JSON)
                .get("/officemanagement/officemanagemententries/2020-11")
                .as(new TypeRef<>() {
                });

        assertThat(result).isEmpty();
    }

    @Test
    @TestSecurity
    @OidcSecurity
    void getAllOfficeManagementEntries_whenNotLogged_thenReturnsHttpStatusUNAUTHORIZED() {
        when(userContext.getUser()).thenReturn(createUserForRole(Role.OFFICE_MANAGEMENT));
        given().contentType(ContentType.JSON)
                .get("/officemanagement/officemanagemententries/2020-11")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void getProjectsWithoutLeads_whenUserIsNotNull_thenReturnDto() {
        final User user = createUserForRole(Role.OFFICE_MANAGEMENT);
        when(userContext.getUser())
                .thenReturn(user);

        when(projectService.getProjectsForMonthYear(any(YearMonth.class), any()))
                .thenReturn(createProjectList());

        List<CustomerProjectWithoutLeadsDto> resultList = given().contentType(ContentType.JSON)
                .get("/officemanagement/projectsWithoutLeads")
                .as(new TypeRef<>() {
                });

        assertThat(resultList).hasSize(2);
        assertThat(resultList.getFirst().getProjectName()).isEqualTo(createProjectList().getFirst().getProjectId());
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

    private Step createStep(StepName stepName) {
        Step step = new Step();
        step.setName(stepName.name());
        return step;
    }

    private List<Project> createProjectList() {
        List<Project> projects = new ArrayList<>();
        projects.add(
                Project.builder()
                        .projectId("ABC")
                        .zepId(1)
                        .leads(List.of())
                        .categories(List.of("CUST"))
                        .build()
        );

        projects.add(
                Project.builder()
                        .projectId("DEF")
                        .zepId(2)
                        .leads(List.of())
                        .categories(List.of("CUST"))
                        .build()
        );

        return projects;
    }
}