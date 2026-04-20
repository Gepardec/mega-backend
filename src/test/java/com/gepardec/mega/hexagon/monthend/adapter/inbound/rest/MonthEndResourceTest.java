package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ApiError;
import com.gepardec.mega.hexagon.generated.model.CreateClarificationRequest;
import com.gepardec.mega.hexagon.generated.model.MonthEndOverviewClarificationEntry;
import com.gepardec.mega.hexagon.generated.model.MonthEndPreparationResponse;
import com.gepardec.mega.hexagon.generated.model.MonthEndStatusOverviewResponse;
import com.gepardec.mega.hexagon.generated.model.MonthEndTaskGenerationResponse;
import com.gepardec.mega.hexagon.generated.model.PrepareMonthEndProjectRequest;
import com.gepardec.mega.hexagon.generated.model.ResolveClarificationRequest;
import com.gepardec.mega.hexagon.generated.model.UpdateClarificationTextRequest;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndTaskUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.DeleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GenerateMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetEmployeeMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetProjectLeadMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.PrematureMonthEndPreparationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.UpdateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndActorNotAuthorizedException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndClarificationNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndTaskNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndPreparationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
class MonthEndResourceTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 3);
    private static final ProjectId PROJECT_ID = ProjectId.of(Instancio.create(UUID.class));
    private static final String PROJECT_NAME = "Test Project";
    private static final UserId EMPLOYEE_ID = UserId.of(Instancio.create(UUID.class));
    private static final UserId PROJECT_LEAD_ID = UserId.of(Instancio.create(UUID.class));
    private static final MonthEndTaskId TASK_ID = MonthEndTaskId.of(Instancio.create(UUID.class));
    private static final MonthEndClarificationId CLARIFICATION_ID = MonthEndClarificationId.of(Instancio.create(UUID.class));
    private static final Instant CREATED_AT = Instant.parse("2026-03-20T08:15:00Z");

    @InjectMock
    AuthenticatedActorContext authenticatedActorContext;

    @InjectMock
    GetEmployeeMonthEndStatusOverviewUseCase getEmployeeMonthEndStatusOverviewUseCase;

    @InjectMock
    GetProjectLeadMonthEndStatusOverviewUseCase getProjectLeadMonthEndStatusOverviewUseCase;

    @InjectMock
    PrematureMonthEndPreparationUseCase prematureMonthEndPreparationUseCase;

    @InjectMock
    CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase;

    @InjectMock
    CompleteMonthEndTaskUseCase completeMonthEndTaskUseCase;

    @InjectMock
    UpdateMonthEndClarificationUseCase updateMonthEndClarificationUseCase;

    @InjectMock
    CompleteMonthEndClarificationUseCase completeMonthEndClarificationUseCase;

    @InjectMock
    DeleteMonthEndClarificationUseCase deleteMonthEndClarificationUseCase;

    @InjectMock
    GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase;

    @InjectMock
    MonthEndUserSnapshotPort userSnapshotPort;

    @InjectMock
    MonthEndProjectSnapshotPort projectSnapshotPort;

    @BeforeEach
    void setUp() {
        when(authenticatedActorContext.userId()).thenReturn(EMPLOYEE_ID);
        when(authenticatedActorContext.hasRole(Role.PROJECT_LEAD)).thenReturn(false);
        when(userSnapshotPort.findByIds(any(), any())).thenReturn(List.of(employeeRef(), projectLeadRef()));
        when(projectSnapshotPort.findByIds(any(), any())).thenReturn(List.of(projectSnapshot()));
    }

    @Test
    void getMonthEndStatusOverview_shouldReturnMappedOverviewForEmployeeRole() {
        allowRoles(Role.EMPLOYEE);
        MonthEndClarification clarification = projectLeadClarification("Please revisit the supporting evidence.");
        MonthEndStatusOverview overview = new MonthEndStatusOverview(
                EMPLOYEE_ID,
                MONTH,
                List.of(MonthEndTask.create(
                        MonthEndTaskId.of(Instancio.create(UUID.class)),
                        MONTH,
                        MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        PROJECT_ID,
                        EMPLOYEE_ID,
                        Set.of(EMPLOYEE_ID)
                )),
                List.of(clarification)
        );
        when(getEmployeeMonthEndStatusOverviewUseCase.getOverview(EMPLOYEE_ID, MONTH)).thenReturn(overview);

        MonthEndStatusOverviewResponse response = given()
                .accept(ContentType.JSON)
                .get("/monthend/{month}/status-overview", MONTH.toString())
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndStatusOverviewResponse.class);

        assertThat(response.getMonth()).isEqualTo(MONTH.toString());
        assertThat(response.getTasks()).singleElement().satisfies(entry -> {
            assertThat(entry.getProject().getId()).isEqualTo(PROJECT_ID.value());
            assertThat(entry.getSubjectEmployee().getId()).isEqualTo(EMPLOYEE_ID.value());
            assertThat(entry.getCanComplete()).isTrue();
        });
        assertThat(response.getClarifications()).singleElement().satisfies(clarificationEntry -> {
            assertThat(clarificationEntry.getSubjectEmployee().getId()).isEqualTo(EMPLOYEE_ID.value());
            assertThat(clarificationEntry.getCreatedBy().getId()).isEqualTo(PROJECT_LEAD_ID.value());
            assertThat(clarificationEntry.getResolvedBy()).isNull();
            assertThat(clarificationEntry.getCanResolve()).isTrue();
            assertThat(clarificationEntry.getCanEditText()).isFalse();
            assertThat(clarificationEntry.getCanDelete()).isFalse();
        });
        verify(getEmployeeMonthEndStatusOverviewUseCase).getOverview(EMPLOYEE_ID, MONTH);
        verifyNoInteractions(getProjectLeadMonthEndStatusOverviewUseCase);
    }

    @Test
    void getMonthEndStatusOverview_shouldReturnMappedOverviewForLeadRole() {
        allowRoles(Role.EMPLOYEE, Role.PROJECT_LEAD);
        when(authenticatedActorContext.userId()).thenReturn(PROJECT_LEAD_ID);
        when(authenticatedActorContext.hasRole(Role.PROJECT_LEAD)).thenReturn(true);
        MonthEndClarification resolvedClarification = employeeClarification("Everything is clarified.")
                .resolve(PROJECT_LEAD_ID, "Confirmed by project lead.", CREATED_AT.plusSeconds(600));
        MonthEndStatusOverview overview = new MonthEndStatusOverview(
                PROJECT_LEAD_ID,
                MONTH,
                List.of(MonthEndTask.create(
                        MonthEndTaskId.of(Instancio.create(UUID.class)),
                        MONTH,
                        MonthEndTaskType.ABRECHNUNG,
                        PROJECT_ID,
                        null,
                        Set.of(PROJECT_LEAD_ID)
                )),
                List.of(resolvedClarification)
        );
        when(getProjectLeadMonthEndStatusOverviewUseCase.getOverview(PROJECT_LEAD_ID, MONTH)).thenReturn(overview);

        MonthEndStatusOverviewResponse response = given()
                .accept(ContentType.JSON)
                .get("/monthend/{month}/status-overview", MONTH.toString())
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndStatusOverviewResponse.class);

        assertThat(response.getMonth()).isEqualTo(MONTH.toString());
        assertThat(response.getTasks()).singleElement().satisfies(entry -> {
            assertThat(entry.getProject().getId()).isEqualTo(PROJECT_ID.value());
            assertThat(entry.getSubjectEmployee()).isNull();
            assertThat(entry.getCanComplete()).isTrue();
        });
        assertThat(response.getClarifications()).singleElement().satisfies(clarificationEntry -> {
            assertThat(clarificationEntry.getSubjectEmployee().getId()).isEqualTo(EMPLOYEE_ID.value());
            assertThat(clarificationEntry.getCreatedBy().getId()).isEqualTo(EMPLOYEE_ID.value());
            assertThat(clarificationEntry.getResolvedBy().getId()).isEqualTo(PROJECT_LEAD_ID.value());
            assertThat(clarificationEntry.getCanResolve()).isFalse();
            assertThat(clarificationEntry.getCanEditText()).isFalse();
            assertThat(clarificationEntry.getCanDelete()).isFalse();
        });
        verify(getProjectLeadMonthEndStatusOverviewUseCase).getOverview(PROJECT_LEAD_ID, MONTH);
        verifyNoInteractions(getEmployeeMonthEndStatusOverviewUseCase);
    }

    @Test
    void prepareMonthEndProject_shouldReturnPreparedTasksAndClarification() {
        allowRoles(Role.EMPLOYEE);
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
                .post("/monthend/preparations")
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndPreparationResponse.class);

        assertThat(response.getEnsuredTasks()).hasSize(2);
        assertThat(response.getClarification()).isNotNull();
        assertThat(response.getClarification().getText()).isEqualTo("Leaving early.");
        assertThat(response.getClarification().getCanEditText()).isTrue();
        assertThat(response.getClarification().getCanDelete()).isTrue();
        verify(prematureMonthEndPreparationUseCase).prepare(MONTH, PROJECT_ID, EMPLOYEE_ID, "Leaving early.");
    }

    @Test
    void createMonthEndClarification_shouldReturnCreatedClarificationForEmployee() {
        allowRoles(Role.EMPLOYEE);
        CreateClarificationRequest request = new CreateClarificationRequest()
                .month(MONTH.toString())
                .projectId(PROJECT_ID.value())
                .text("Need support.");
        MonthEndClarification clarification = employeeClarification("Need support.");
        when(createMonthEndClarificationUseCase.create(
                MONTH,
                PROJECT_ID,
                EMPLOYEE_ID,
                EMPLOYEE_ID,
                "Need support."
        )).thenReturn(clarification);

        MonthEndOverviewClarificationEntry response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .post("/monthend/clarifications")
                .then()
                .statusCode(201)
                .extract()
                .as(MonthEndOverviewClarificationEntry.class);

        assertThat(response.getProjectId()).isEqualTo(PROJECT_ID.value());
        assertThat(response.getCreatedBy().getId()).isEqualTo(EMPLOYEE_ID.value());
        assertThat(response.getSubjectEmployee().getId()).isEqualTo(EMPLOYEE_ID.value());
        assertThat(response.getText()).isEqualTo("Need support.");
        assertThat(response.getCanEditText()).isTrue();
        assertThat(response.getCanDelete()).isTrue();
        assertThat(response.getCanResolve()).isFalse();
        verify(createMonthEndClarificationUseCase).create(
                MONTH,
                PROJECT_ID,
                EMPLOYEE_ID,
                EMPLOYEE_ID,
                "Need support."
        );
    }

    @Test
    void createMonthEndClarification_shouldReturnCreatedClarificationForLeadWithSubjectEmployee() {
        allowRoles(Role.EMPLOYEE, Role.PROJECT_LEAD);
        when(authenticatedActorContext.userId()).thenReturn(PROJECT_LEAD_ID);
        when(authenticatedActorContext.hasRole(Role.PROJECT_LEAD)).thenReturn(true);
        CreateClarificationRequest request = new CreateClarificationRequest()
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
                "Please fix the evidence."
        )).thenReturn(clarification);

        MonthEndOverviewClarificationEntry response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .post("/monthend/clarifications")
                .then()
                .statusCode(201)
                .extract()
                .as(MonthEndOverviewClarificationEntry.class);

        assertThat(response.getProjectId()).isEqualTo(PROJECT_ID.value());
        assertThat(response.getCreatedBy().getId()).isEqualTo(PROJECT_LEAD_ID.value());
        assertThat(response.getSubjectEmployee().getId()).isEqualTo(EMPLOYEE_ID.value());
        assertThat(response.getText()).isEqualTo("Please fix the evidence.");
        verify(createMonthEndClarificationUseCase).create(
                MONTH,
                PROJECT_ID,
                EMPLOYEE_ID,
                PROJECT_LEAD_ID,
                "Please fix the evidence."
        );
    }

    @Test
    void createMonthEndClarification_shouldUseActorAsSubjectWhenLeadOmitsSubjectEmployee() {
        allowRoles(Role.EMPLOYEE, Role.PROJECT_LEAD);
        when(authenticatedActorContext.userId()).thenReturn(PROJECT_LEAD_ID);
        when(authenticatedActorContext.hasRole(Role.PROJECT_LEAD)).thenReturn(true);
        CreateClarificationRequest request = new CreateClarificationRequest()
                .month(MONTH.toString())
                .projectId(PROJECT_ID.value())
                .text("Project-level follow-up.");
        MonthEndClarification clarification = leadSelfClarification("Project-level follow-up.");
        when(createMonthEndClarificationUseCase.create(
                MONTH,
                PROJECT_ID,
                PROJECT_LEAD_ID,
                PROJECT_LEAD_ID,
                "Project-level follow-up."
        )).thenReturn(clarification);

        MonthEndOverviewClarificationEntry response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .post("/monthend/clarifications")
                .then()
                .statusCode(201)
                .extract()
                .as(MonthEndOverviewClarificationEntry.class);

        assertThat(response.getCreatedBy().getId()).isEqualTo(PROJECT_LEAD_ID.value());
        assertThat(response.getSubjectEmployee().getId()).isEqualTo(PROJECT_LEAD_ID.value());
        assertThat(response.getText()).isEqualTo("Project-level follow-up.");
        verify(createMonthEndClarificationUseCase).create(
                MONTH,
                PROJECT_ID,
                PROJECT_LEAD_ID,
                PROJECT_LEAD_ID,
                "Project-level follow-up."
        );
    }

    @Test
    void completeMonthEndTask_shouldReturnNotFoundErrorBodyWhenUseCaseSignalsMissingTask() {
        allowRoles(Role.EMPLOYEE);
        when(completeMonthEndTaskUseCase.complete(TASK_ID, EMPLOYEE_ID))
                .thenThrow(new MonthEndTaskNotFoundException("month-end task not found"));

        ApiError response = given()
                .accept(ContentType.JSON)
                .post("/monthend/tasks/{taskId}/complete", TASK_ID.value())
                .then()
                .statusCode(404)
                .extract()
                .as(ApiError.class);

        assertThat(response.getMessage()).isEqualTo("month-end task not found");
    }

    @Test
    void updateMonthEndClarificationText_shouldReturnUpdatedClarification() {
        allowRoles(Role.EMPLOYEE, Role.PROJECT_LEAD);
        when(authenticatedActorContext.userId()).thenReturn(PROJECT_LEAD_ID);
        MonthEndClarification clarification = projectLeadClarification("Updated by lead.");
        when(updateMonthEndClarificationUseCase.updateText(CLARIFICATION_ID, PROJECT_LEAD_ID, "Updated by lead."))
                .thenReturn(clarification);

        MonthEndOverviewClarificationEntry response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new UpdateClarificationTextRequest().text("Updated by lead."))
                .put("/monthend/clarifications/{clarificationId}/text", CLARIFICATION_ID.value())
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndOverviewClarificationEntry.class);

        assertThat(response.getClarificationId()).isEqualTo(CLARIFICATION_ID.value());
        assertThat(response.getCreatedBy().getId()).isEqualTo(PROJECT_LEAD_ID.value());
        assertThat(response.getText()).isEqualTo("Updated by lead.");
        assertThat(response.getCanEditText()).isTrue();
        assertThat(response.getCanDelete()).isTrue();
        assertThat(response.getCanResolve()).isFalse();
        verify(updateMonthEndClarificationUseCase).updateText(CLARIFICATION_ID, PROJECT_LEAD_ID, "Updated by lead.");
    }

    @Test
    void resolveMonthEndClarification_shouldReturnResolvedClarificationForLeadRole() {
        allowRoles(Role.EMPLOYEE, Role.PROJECT_LEAD);
        when(authenticatedActorContext.userId()).thenReturn(PROJECT_LEAD_ID);
        MonthEndClarification clarification = employeeClarification("Need help.")
                .resolve(PROJECT_LEAD_ID, "Handled.", Instant.parse("2026-03-21T10:30:00Z"));
        when(completeMonthEndClarificationUseCase.complete(CLARIFICATION_ID, PROJECT_LEAD_ID, "Handled."))
                .thenReturn(clarification);

        MonthEndOverviewClarificationEntry response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new ResolveClarificationRequest().resolutionNote("Handled."))
                .post("/monthend/clarifications/{clarificationId}/resolve", CLARIFICATION_ID.value())
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndOverviewClarificationEntry.class);

        assertThat(response.getClarificationId()).isEqualTo(CLARIFICATION_ID.value());
        assertThat(response.getResolvedBy().getId()).isEqualTo(PROJECT_LEAD_ID.value());
        assertThat(response.getResolutionNote()).isEqualTo("Handled.");
        assertThat(response.getCanResolve()).isFalse();
        assertThat(response.getCanEditText()).isFalse();
        assertThat(response.getCanDelete()).isFalse();
        verify(completeMonthEndClarificationUseCase).complete(CLARIFICATION_ID, PROJECT_LEAD_ID, "Handled.");
    }

    @Test
    void deleteMonthEndClarification_shouldReturn204WhenCreatorDeletesOwnClarification() {
        allowRoles(Role.EMPLOYEE);

        given()
                .delete("/monthend/clarifications/{clarificationId}", CLARIFICATION_ID.value())
                .then()
                .statusCode(204);

        verify(deleteMonthEndClarificationUseCase).delete(CLARIFICATION_ID, EMPLOYEE_ID);
    }

    @Test
    void deleteMonthEndClarification_shouldReturn403WhenNonCreatorAttemptsDelete() {
        allowRoles(Role.EMPLOYEE);
        doThrow(new MonthEndActorNotAuthorizedException("actor is not allowed to delete this clarification"))
                .when(deleteMonthEndClarificationUseCase).delete(CLARIFICATION_ID, EMPLOYEE_ID);

        given()
                .delete("/monthend/clarifications/{clarificationId}", CLARIFICATION_ID.value())
                .then()
                .statusCode(403);
    }

    @Test
    void deleteMonthEndClarification_shouldReturn404WhenClarificationNotFound() {
        allowRoles(Role.EMPLOYEE);
        doThrow(new MonthEndClarificationNotFoundException("clarification not found: " + CLARIFICATION_ID.value()))
                .when(deleteMonthEndClarificationUseCase).delete(CLARIFICATION_ID, EMPLOYEE_ID);

        given()
                .delete("/monthend/clarifications/{clarificationId}", CLARIFICATION_ID.value())
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "cron", roles = "mega-cron:sync")
    void generateMonthEndTasks_shouldReturnGenerationResultForCronRole() {
        MonthEndTaskGenerationResult result = new MonthEndTaskGenerationResult(MONTH, 4, 2);
        when(generateMonthEndTasksUseCase.generate(MONTH)).thenReturn(result);

        MonthEndTaskGenerationResponse response = given()
                .accept(ContentType.JSON)
                .post("/monthend/{month}/generate", MONTH.toString())
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndTaskGenerationResponse.class);

        assertThat(response.getMonth()).isEqualTo(MONTH.toString());
        assertThat(response.getCreated()).isEqualTo(4);
        assertThat(response.getSkipped()).isEqualTo(2);
        verify(generateMonthEndTasksUseCase).generate(MONTH);
    }

    @Test
    @TestSecurity(user = "cron")
    void generateMonthEndTasks_shouldRejectMissingCronRole() {
        given()
                .accept(ContentType.JSON)
                .post("/monthend/{month}/generate", MONTH.toString())
                .then()
                .statusCode(403);

        verifyNoInteractions(generateMonthEndTasksUseCase);
    }

    private void allowRoles(Role... roles) {
        when(authenticatedActorContext.roles()).thenReturn(Set.of(roles));
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
                CLARIFICATION_ID,
                MONTH,
                PROJECT_ID,
                EMPLOYEE_ID,
                EMPLOYEE_ID,
                Set.of(PROJECT_LEAD_ID),
                text,
                CREATED_AT
        );
    }

    private MonthEndClarification projectLeadClarification(String text) {
        return MonthEndClarification.create(
                CLARIFICATION_ID,
                MONTH,
                PROJECT_ID,
                EMPLOYEE_ID,
                PROJECT_LEAD_ID,
                Set.of(PROJECT_LEAD_ID),
                text,
                CREATED_AT
        );
    }

    private MonthEndClarification leadSelfClarification(String text) {
        return MonthEndClarification.create(
                CLARIFICATION_ID,
                MONTH,
                PROJECT_ID,
                PROJECT_LEAD_ID,
                PROJECT_LEAD_ID,
                Set.of(PROJECT_LEAD_ID),
                text,
                CREATED_AT
        );
    }

    private MonthEndProjectSnapshot projectSnapshot() {
        return new MonthEndProjectSnapshot(PROJECT_ID, 77, PROJECT_NAME, true, Set.of(PROJECT_LEAD_ID));
    }

    private UserRef employeeRef() {
        return new UserRef(EMPLOYEE_ID, FullName.of("Test", "Employee"), ZepUsername.of("test.employee"));
    }

    private UserRef projectLeadRef() {
        return new UserRef(PROJECT_LEAD_ID, FullName.of("Test", "Project Lead"), ZepUsername.of("test.projectlead"));
    }
}
