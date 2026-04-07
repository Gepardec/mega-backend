package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.ApiError;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndClarificationResponse;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndStatusOverviewResponse;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.ResolveClarificationRequest;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.UpdateClarificationTextRequest;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndTaskNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndEmployee;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProject;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.CompleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.CompleteMonthEndTaskUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.UpdateMonthEndClarificationUseCase;
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
        @Claim(key = "email", value = MonthEndSharedResourceTest.TEST_EMAIL)
})
class MonthEndSharedResourceTest {

    static final String TEST_EMAIL = "test@gepardec.com";

    private static final YearMonth MONTH = YearMonth.of(2026, 3);
    private static final ProjectId PROJECT_ID = ProjectId.of(Instancio.create(UUID.class));
    private static final String PROJECT_NAME = "Project Shared";
    private static final UserId EMPLOYEE_ID = UserId.of(Instancio.create(UUID.class));
    private static final String EMPLOYEE_NAME = "Shared Employee";
    private static final UserId PROJECT_LEAD_ID = UserId.of(Instancio.create(UUID.class));
    private static final MonthEndTaskId TASK_ID = MonthEndTaskId.of(Instancio.create(UUID.class));
    private static final MonthEndClarificationId CLARIFICATION_ID = MonthEndClarificationId.of(Instancio.create(UUID.class));
    private static final Instant CREATED_AT = Instant.parse("2026-03-20T08:15:00Z");

    @InjectMock
    UserService userService;

    @InjectMock
    CurrentMonthEndRestActorResolver currentMonthEndRestActorResolver;

    @InjectMock
    GetMonthEndStatusOverviewUseCase getMonthEndStatusOverviewUseCase;

    @InjectMock
    CompleteMonthEndTaskUseCase completeMonthEndTaskUseCase;

    @InjectMock
    UpdateMonthEndClarificationUseCase updateMonthEndClarificationUseCase;

    @InjectMock
    CompleteMonthEndClarificationUseCase completeMonthEndClarificationUseCase;

    @BeforeEach
    void setUp() {
        when(currentMonthEndRestActorResolver.resolveCurrentActorId()).thenReturn(EMPLOYEE_ID);
    }

    @Test
    void getMonthEndStatusOverview_shouldReturnMappedOverviewForEmployeeRole() {
        allowLegacyRoles(Role.EMPLOYEE);
        MonthEndStatusOverview overview = new MonthEndStatusOverview(
                EMPLOYEE_ID,
                MONTH,
                List.of(new MonthEndStatusOverviewItem(
                        TASK_ID,
                        MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        MonthEndTaskStatus.DONE,
                        new MonthEndProject(PROJECT_ID, PROJECT_NAME),
                        new MonthEndEmployee(EMPLOYEE_ID, EMPLOYEE_NAME),
                        EMPLOYEE_ID
                ))
        );
        when(getMonthEndStatusOverviewUseCase.getOverview(EMPLOYEE_ID, MONTH)).thenReturn(overview);

        MonthEndStatusOverviewResponse response = given()
                .accept(ContentType.JSON)
                .queryParam("month", MONTH.toString())
                .get("/monthend/status-overview")
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndStatusOverviewResponse.class);

        assertThat(response.getMonth()).isEqualTo(MONTH.toString());
        assertThat(response.getEntries()).singleElement().satisfies(entry -> {
            assertThat(entry.getTaskId()).isEqualTo(TASK_ID.value());
            assertThat(entry.getProject().getId()).isEqualTo(PROJECT_ID.value());
            assertThat(entry.getProject().getName()).isEqualTo(PROJECT_NAME);
            assertThat(entry.getSubjectEmployee()).isNotNull();
            assertThat(entry.getSubjectEmployee().getId()).isEqualTo(EMPLOYEE_ID.value());
            assertThat(entry.getSubjectEmployee().getFullName()).isEqualTo(EMPLOYEE_NAME);
            assertThat(entry.getCompletedBy()).isEqualTo(EMPLOYEE_ID.value());
        });
        verify(getMonthEndStatusOverviewUseCase).getOverview(EMPLOYEE_ID, MONTH);
    }

    @Test
    void getMonthEndStatusOverview_shouldOmitSubjectEmployeeForAbrechnungEntry() {
        allowLegacyRoles(Role.PROJECT_LEAD);
        when(currentMonthEndRestActorResolver.resolveCurrentActorId()).thenReturn(PROJECT_LEAD_ID);
        MonthEndStatusOverview overview = new MonthEndStatusOverview(
                PROJECT_LEAD_ID,
                MONTH,
                List.of(new MonthEndStatusOverviewItem(
                        TASK_ID,
                        MonthEndTaskType.ABRECHNUNG,
                        MonthEndTaskStatus.OPEN,
                        new MonthEndProject(PROJECT_ID, PROJECT_NAME),
                        null,
                        null
                ))
        );
        when(getMonthEndStatusOverviewUseCase.getOverview(PROJECT_LEAD_ID, MONTH)).thenReturn(overview);

        MonthEndStatusOverviewResponse response = given()
                .accept(ContentType.JSON)
                .queryParam("month", MONTH.toString())
                .get("/monthend/status-overview")
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndStatusOverviewResponse.class);

        assertThat(response.getEntries()).singleElement()
                .satisfies(entry -> assertThat(entry.getSubjectEmployee()).isNull());
        verify(getMonthEndStatusOverviewUseCase).getOverview(PROJECT_LEAD_ID, MONTH);
    }

    @Test
    void completeMonthEndTask_shouldReturnNotFoundErrorBodyWhenUseCaseSignalsMissingTask() {
        allowLegacyRoles(Role.EMPLOYEE);
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
        allowLegacyRoles(Role.PROJECT_LEAD);
        when(currentMonthEndRestActorResolver.resolveCurrentActorId()).thenReturn(PROJECT_LEAD_ID);
        MonthEndClarification clarification = leadCreatedClarification("Updated by lead.");
        when(updateMonthEndClarificationUseCase.updateText(CLARIFICATION_ID, PROJECT_LEAD_ID, "Updated by lead."))
                .thenReturn(clarification);

        MonthEndClarificationResponse response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new UpdateClarificationTextRequest().text("Updated by lead."))
                .put("/monthend/clarifications/{clarificationId}/text", CLARIFICATION_ID.value())
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndClarificationResponse.class);

        assertThat(response.getClarificationId()).isEqualTo(CLARIFICATION_ID.value());
        assertThat(response.getCreatedBy()).isEqualTo(PROJECT_LEAD_ID.value());
        assertThat(response.getText()).isEqualTo("Updated by lead.");
        verify(updateMonthEndClarificationUseCase).updateText(CLARIFICATION_ID, PROJECT_LEAD_ID, "Updated by lead.");
    }

    @Test
    void resolveMonthEndClarification_shouldReturnResolvedClarificationForLeadRole() {
        allowLegacyRoles(Role.PROJECT_LEAD);
        when(currentMonthEndRestActorResolver.resolveCurrentActorId()).thenReturn(PROJECT_LEAD_ID);
        MonthEndClarification clarification = employeeCreatedClarification("Need help.")
                .resolve(PROJECT_LEAD_ID, "Handled.", Instant.parse("2026-03-21T10:30:00Z"));
        when(completeMonthEndClarificationUseCase.complete(CLARIFICATION_ID, PROJECT_LEAD_ID, "Handled."))
                .thenReturn(clarification);

        MonthEndClarificationResponse response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new ResolveClarificationRequest().resolutionNote("Handled."))
                .post("/monthend/clarifications/{clarificationId}/resolve", CLARIFICATION_ID.value())
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndClarificationResponse.class);

        assertThat(response.getClarificationId()).isEqualTo(CLARIFICATION_ID.value());
        assertThat(response.getResolvedBy()).isEqualTo(PROJECT_LEAD_ID.value());
        assertThat(response.getResolutionNote()).isEqualTo("Handled.");
        verify(completeMonthEndClarificationUseCase).complete(CLARIFICATION_ID, PROJECT_LEAD_ID, "Handled.");
    }

    @Test
    void getMonthEndStatusOverview_shouldRejectOfficeManagementRole() {
        allowLegacyRoles(Role.OFFICE_MANAGEMENT);

        given()
                .accept(ContentType.JSON)
                .queryParam("month", MONTH.toString())
                .get("/monthend/status-overview")
                .then()
                .statusCode(403);

        verifyNoInteractions(getMonthEndStatusOverviewUseCase);
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

    private MonthEndClarification employeeCreatedClarification(String text) {
        return MonthEndClarification.create(
                CLARIFICATION_ID,
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

    private MonthEndClarification leadCreatedClarification(String text) {
        return MonthEndClarification.create(
                CLARIFICATION_ID,
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
}
