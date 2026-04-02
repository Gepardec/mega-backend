package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.CreateEmployeeClarificationRequest;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.CreateProjectLeadClarificationRequest;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndClarificationResponse;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndPreparationResponse;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndWorklistResponse;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.PrepareMonthEndProjectRequest;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndPreparationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistClarificationItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistItem;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetEmployeeMonthEndWorklistUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetProjectLeadMonthEndWorklistUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.PrematureMonthEndPreparationUseCase;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.service.api.UserService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
@OidcSecurity(claims = {
        @Claim(key = "email", value = MonthEndEmployeeAndProjectLeadResourceTest.TEST_EMAIL)
})
class MonthEndEmployeeAndProjectLeadResourceTest {

    static final String TEST_EMAIL = "test@gepardec.com";

    private static final YearMonth MONTH = YearMonth.of(2026, 3);
    private static final ProjectId PROJECT_ID = ProjectId.of(Instancio.create(UUID.class));
    private static final UserId EMPLOYEE_ID = UserId.of(Instancio.create(UUID.class));
    private static final UserId PROJECT_LEAD_ID = UserId.of(Instancio.create(UUID.class));
    private static final Instant CREATED_AT = Instant.parse("2026-03-20T08:15:00Z");

    @InjectMock
    UserService userService;

    @InjectMock
    CurrentMonthEndRestActorResolver currentMonthEndRestActorResolver;

    @InjectMock
    GetEmployeeMonthEndWorklistUseCase getEmployeeMonthEndWorklistUseCase;

    @InjectMock
    PrematureMonthEndPreparationUseCase prematureMonthEndPreparationUseCase;

    @InjectMock
    CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase;

    @InjectMock
    GetProjectLeadMonthEndWorklistUseCase getProjectLeadMonthEndWorklistUseCase;

    @BeforeEach
    void setUp() {
        when(currentMonthEndRestActorResolver.resolveCurrentActorId()).thenReturn(EMPLOYEE_ID);
    }

    @Test
    void getEmployeeMonthEndWorklist_shouldReturnMappedWorklistForEmployeeRole() {
        allowLegacyRoles(Role.EMPLOYEE);
        MonthEndWorklist worklist = new MonthEndWorklist(
                EMPLOYEE_ID,
                MONTH,
                List.of(new MonthEndWorklistItem(
                        MonthEndTaskId.of(Instancio.create(UUID.class)),
                        MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        PROJECT_ID,
                        EMPLOYEE_ID
                )),
                List.of(toWorklistItem(employeeClarification("Please review the booking.")))
        );
        when(getEmployeeMonthEndWorklistUseCase.getWorklist(EMPLOYEE_ID, MONTH)).thenReturn(worklist);

        MonthEndWorklistResponse response = given()
                .accept(ContentType.JSON)
                .queryParam("month", MONTH.toString())
                .get("/monthend/employee/worklist")
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndWorklistResponse.class);

        assertThat(response.getMonth()).isEqualTo(MONTH.toString());
        assertThat(response.getTasks()).singleElement().satisfies(task -> {
            assertThat(task.getProjectId()).isEqualTo(PROJECT_ID.value());
            assertThat(task.getSubjectEmployeeId()).isEqualTo(EMPLOYEE_ID.value());
        });
        assertThat(response.getClarifications()).singleElement().satisfies(clarification -> {
            assertThat(clarification.getProjectId()).isEqualTo(PROJECT_ID.value());
            assertThat(clarification.getCreatedBy()).isEqualTo(EMPLOYEE_ID.value());
            assertThat(clarification.getText()).isEqualTo("Please review the booking.");
        });
        verify(getEmployeeMonthEndWorklistUseCase).getWorklist(EMPLOYEE_ID, MONTH);
    }

    @Test
    void prepareEmployeeMonthEndProject_shouldReturnPreparedTasksAndClarification() {
        allowLegacyRoles(Role.EMPLOYEE);
        PrepareMonthEndProjectRequest request = new PrepareMonthEndProjectRequest()
                .month(MONTH.toString())
                .projectId(PROJECT_ID.value())
                .clarificationText("Leaving early.");
        MonthEndPreparationResult result = new MonthEndPreparationResult(
                List.of(
                        employeeTask(MonthEndTaskType.EMPLOYEE_TIME_CHECK),
                        employeeTask(MonthEndTaskType.LEISTUNGSNACHWEIS)
                ),
                employeeClarification("Leaving early.")
        );
        when(prematureMonthEndPreparationUseCase.prepare(MONTH, PROJECT_ID, EMPLOYEE_ID, "Leaving early."))
                .thenReturn(result);

        MonthEndPreparationResponse response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .post("/monthend/employee/preparations")
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndPreparationResponse.class);

        assertThat(response.getEnsuredTasks()).hasSize(2);
        assertThat(response.getClarification()).isNotNull();
        assertThat(response.getClarification().getText()).isEqualTo("Leaving early.");
        verify(prematureMonthEndPreparationUseCase).prepare(MONTH, PROJECT_ID, EMPLOYEE_ID, "Leaving early.");
    }

    @Test
    void createEmployeeMonthEndClarification_shouldReturnCreatedClarification() {
        allowLegacyRoles(Role.EMPLOYEE);
        CreateEmployeeClarificationRequest request = new CreateEmployeeClarificationRequest()
                .month(MONTH.toString())
                .projectId(PROJECT_ID.value())
                .text("Need support.");
        MonthEndClarification clarification = employeeClarification("Need support.");
        when(createMonthEndClarificationUseCase.create(
                MONTH,
                PROJECT_ID,
                EMPLOYEE_ID,
                EMPLOYEE_ID,
                MonthEndClarificationSide.EMPLOYEE,
                "Need support."
        )).thenReturn(clarification);

        MonthEndClarificationResponse response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .post("/monthend/employee/clarifications")
                .then()
                .statusCode(201)
                .extract()
                .as(MonthEndClarificationResponse.class);

        assertThat(response.getProjectId()).isEqualTo(PROJECT_ID.value());
        assertThat(response.getCreatedBy()).isEqualTo(EMPLOYEE_ID.value());
        assertThat(response.getSubjectEmployeeId()).isEqualTo(EMPLOYEE_ID.value());
        assertThat(response.getText()).isEqualTo("Need support.");
    }

    @Test
    void getProjectLeadMonthEndWorklist_shouldReturnMappedWorklistForLeadRole() {
        allowLegacyRoles(Role.PROJECT_LEAD);
        when(currentMonthEndRestActorResolver.resolveCurrentActorId()).thenReturn(PROJECT_LEAD_ID);
        MonthEndWorklist worklist = new MonthEndWorklist(
                PROJECT_LEAD_ID,
                MONTH,
                List.of(new MonthEndWorklistItem(
                        MonthEndTaskId.of(Instancio.create(UUID.class)),
                        MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        PROJECT_ID,
                        EMPLOYEE_ID
                )),
                List.of()
        );
        when(getProjectLeadMonthEndWorklistUseCase.getWorklist(PROJECT_LEAD_ID, MONTH)).thenReturn(worklist);

        MonthEndWorklistResponse response = given()
                .accept(ContentType.JSON)
                .queryParam("month", MONTH.toString())
                .get("/monthend/project-lead/worklist")
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndWorklistResponse.class);

        assertThat(response.getMonth()).isEqualTo(MONTH.toString());
        assertThat(response.getTasks()).singleElement().satisfies(task -> {
            assertThat(task.getProjectId()).isEqualTo(PROJECT_ID.value());
            assertThat(task.getSubjectEmployeeId()).isEqualTo(EMPLOYEE_ID.value());
        });
        verify(getProjectLeadMonthEndWorklistUseCase).getWorklist(PROJECT_LEAD_ID, MONTH);
    }

    @Test
    void createProjectLeadMonthEndClarification_shouldReturnCreatedClarification() {
        allowLegacyRoles(Role.PROJECT_LEAD);
        when(currentMonthEndRestActorResolver.resolveCurrentActorId()).thenReturn(PROJECT_LEAD_ID);
        CreateProjectLeadClarificationRequest request = new CreateProjectLeadClarificationRequest()
                .month(MONTH.toString())
                .projectId(PROJECT_ID.value())
                .subjectEmployeeId(EMPLOYEE_ID.value())
                .text("Please fix the evidence.");
        MonthEndClarification clarification = projectLeadClarification("Please fix the evidence.");
        when(createMonthEndClarificationUseCase.create(
                MONTH,
                PROJECT_ID,
                EMPLOYEE_ID,
                PROJECT_LEAD_ID,
                MonthEndClarificationSide.PROJECT_LEAD,
                "Please fix the evidence."
        )).thenReturn(clarification);

        MonthEndClarificationResponse response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .post("/monthend/project-lead/clarifications")
                .then()
                .statusCode(201)
                .extract()
                .as(MonthEndClarificationResponse.class);

        assertThat(response.getProjectId()).isEqualTo(PROJECT_ID.value());
        assertThat(response.getCreatedBy()).isEqualTo(PROJECT_LEAD_ID.value());
        assertThat(response.getSubjectEmployeeId()).isEqualTo(EMPLOYEE_ID.value());
        assertThat(response.getText()).isEqualTo("Please fix the evidence.");
    }

    @Test
    void getProjectLeadMonthEndWorklist_shouldRejectEmployeeRole() {
        allowLegacyRoles(Role.EMPLOYEE);

        given()
                .accept(ContentType.JSON)
                .queryParam("month", MONTH.toString())
                .get("/monthend/project-lead/worklist")
                .then()
                .statusCode(403);

        verifyNoInteractions(getProjectLeadMonthEndWorklistUseCase);
    }

    private void allowLegacyRoles(Role... roles) {
        when(userService.findUserForEmail(TEST_EMAIL)).thenReturn(com.gepardec.mega.domain.model.User.builder()
                .dbId(1L)
                .userId("legacy-user")
                .email(TEST_EMAIL)
                .firstname("Test")
                .lastname("User")
                .roles(Set.of(roles))
                .build());
    }

    private MonthEndTask employeeTask(MonthEndTaskType type) {
        return MonthEndTask.create(
                MonthEndTaskId.of(Instancio.create(UUID.class)),
                MONTH,
                type,
                PROJECT_ID,
                EMPLOYEE_ID,
                Set.of(EMPLOYEE_ID)
        );
    }

    private MonthEndClarification employeeClarification(String text) {
        return MonthEndClarification.create(
                com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId.of(Instancio.create(UUID.class)),
                MONTH,
                PROJECT_ID,
                EMPLOYEE_ID,
                EMPLOYEE_ID,
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(PROJECT_LEAD_ID),
                text,
                CREATED_AT
        );
    }

    private MonthEndClarification projectLeadClarification(String text) {
        return MonthEndClarification.create(
                com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId.of(Instancio.create(UUID.class)),
                MONTH,
                PROJECT_ID,
                EMPLOYEE_ID,
                PROJECT_LEAD_ID,
                MonthEndClarificationSide.PROJECT_LEAD,
                Set.of(PROJECT_LEAD_ID),
                text,
                CREATED_AT
        );
    }

    private MonthEndWorklistClarificationItem toWorklistItem(MonthEndClarification clarification) {
        return new MonthEndWorklistClarificationItem(
                clarification.id(),
                clarification.projectId(),
                clarification.subjectEmployeeId(),
                clarification.createdBy(),
                clarification.creatorSide(),
                MonthEndClarificationStatus.OPEN,
                clarification.text(),
                clarification.createdAt(),
                clarification.lastModifiedAt()
        );
    }
}
