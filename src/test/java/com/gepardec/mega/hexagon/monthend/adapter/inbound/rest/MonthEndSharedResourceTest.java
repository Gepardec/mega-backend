package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ApiError;
import com.gepardec.mega.hexagon.generated.model.MonthEndClarificationResponse;
import com.gepardec.mega.hexagon.generated.model.ResolveClarificationRequest;
import com.gepardec.mega.hexagon.generated.model.UpdateClarificationTextRequest;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndTaskUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.DeleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.UpdateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndActorNotAuthorizedException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndClarificationNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndTaskNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
class MonthEndSharedResourceTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 3);
    private static final ProjectId PROJECT_ID = ProjectId.of(Instancio.create(UUID.class));
    private static final UserId EMPLOYEE_ID = UserId.of(Instancio.create(UUID.class));
    private static final UserId PROJECT_LEAD_ID = UserId.of(Instancio.create(UUID.class));
    private static final MonthEndTaskId TASK_ID = MonthEndTaskId.of(Instancio.create(UUID.class));
    private static final MonthEndClarificationId CLARIFICATION_ID = MonthEndClarificationId.of(Instancio.create(UUID.class));
    private static final Instant CREATED_AT = Instant.parse("2026-03-20T08:15:00Z");

    @InjectMock
    AuthenticatedActorContext authenticatedActorContext;

    @InjectMock
    CompleteMonthEndTaskUseCase completeMonthEndTaskUseCase;

    @InjectMock
    UpdateMonthEndClarificationUseCase updateMonthEndClarificationUseCase;

    @InjectMock
    CompleteMonthEndClarificationUseCase completeMonthEndClarificationUseCase;

    @InjectMock
    DeleteMonthEndClarificationUseCase deleteMonthEndClarificationUseCase;

    @BeforeEach
    void setUp() {
        when(authenticatedActorContext.userId()).thenReturn(EMPLOYEE_ID);
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
        allowRoles(Role.PROJECT_LEAD);
        when(authenticatedActorContext.userId()).thenReturn(PROJECT_LEAD_ID);
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
        allowRoles(Role.PROJECT_LEAD);
        when(authenticatedActorContext.userId()).thenReturn(PROJECT_LEAD_ID);
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
    void deleteMonthEndClarification_shouldReturn204_whenCreatorDeletesOwnClarification() {
        allowRoles(Role.EMPLOYEE);

        given()
                .delete("/monthend/clarifications/{clarificationId}", CLARIFICATION_ID.value())
                .then()
                .statusCode(204);

        verify(deleteMonthEndClarificationUseCase).delete(CLARIFICATION_ID, EMPLOYEE_ID);
    }

    @Test
    void deleteMonthEndClarification_shouldReturn403_whenNonCreatorAttemptsDelete() {
        allowRoles(Role.EMPLOYEE);
        doThrow(new MonthEndActorNotAuthorizedException("actor is not allowed to delete this clarification"))
                .when(deleteMonthEndClarificationUseCase).delete(CLARIFICATION_ID, EMPLOYEE_ID);

        given()
                .delete("/monthend/clarifications/{clarificationId}", CLARIFICATION_ID.value())
                .then()
                .statusCode(403);
    }

    @Test
    void deleteMonthEndClarification_shouldReturn404_whenClarificationNotFound() {
        allowRoles(Role.EMPLOYEE);
        doThrow(new MonthEndClarificationNotFoundException("clarification not found: " + CLARIFICATION_ID.value()))
                .when(deleteMonthEndClarificationUseCase).delete(CLARIFICATION_ID, EMPLOYEE_ID);

        given()
                .delete("/monthend/clarifications/{clarificationId}", CLARIFICATION_ID.value())
                .then()
                .statusCode(404);
    }

    private void allowRoles(Role... roles) {
        when(authenticatedActorContext.roles()).thenReturn(Set.of(roles));
    }

    private MonthEndClarification employeeCreatedClarification(String text) {
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

    private MonthEndClarification leadCreatedClarification(String text) {
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
}
