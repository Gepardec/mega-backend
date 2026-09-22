package com.gepardec.mega.hexagon.shared.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.LeistungsnachweisToggleRequestDto;
import com.gepardec.mega.hexagon.generated.model.ProblemDto;
import com.gepardec.mega.hexagon.generated.model.ViolationDto;
import com.gepardec.mega.hexagon.project.application.port.inbound.GetProjectSettingsUseCase;
import com.gepardec.mega.hexagon.project.application.port.inbound.SetLeistungsnachweisEnabledUseCase;
import com.gepardec.mega.hexagon.project.domain.error.ProjectErrorCode;
import com.gepardec.mega.hexagon.project.domain.error.ProjectException;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

@QuarkusTest
class ProblemContractTest {

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final UserId ACTOR_ID = UserId.of(UUID.randomUUID());
    private static final ProjectId PROJECT_ID = ProjectId.of(UUID.randomUUID());

    @InjectMock
    AuthenticatedActorContext authenticatedActorContext;

    @InjectMock
    GetProjectSettingsUseCase getProjectSettingsUseCase;

    @InjectMock
    SetLeistungsnachweisEnabledUseCase setLeistungsnachweisEnabledUseCase;

    @BeforeEach
    void setUp() {
        when(authenticatedActorContext.userId()).thenReturn(ACTOR_ID);
    }

    @Test
    @TestSecurity(user = "test")
    void domainNotFound_shouldBeProblemWithCodeAndDetail() {
        allowRoles(Role.PROJECT_LEAD);
        when(setLeistungsnachweisEnabledUseCase.setLeistungsnachweisEnabled(PROJECT_ID, ACTOR_ID, true))
                .thenThrow(new ProjectException(ProjectErrorCode.NOT_FOUND, "project not found: " + PROJECT_ID.value()));
        String path = "/projects/" + PROJECT_ID.value() + "/leistungsnachweis-enabled";

        Response response = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(new LeistungsnachweisToggleRequestDto().enabled(true))
                .put(path);

        ProblemDto problem = problemOf(response, 404);
        assertThat(problem.getTitle()).isEqualTo("Not Found");
        assertThat(problem.getInstance()).isEqualTo(path);
        assertThat(problem.getCode()).isEqualTo("PROJECT_NOT_FOUND");
        assertThat(problem.getDetail()).isEqualTo("project not found: " + PROJECT_ID.value());
        assertThat(response.jsonPath().getMap("$")).doesNotContainKey("type");
    }

    @Test
    @TestSecurity(user = "test")
    void adapterValidation_shouldBeProblemWithPathViolationAndNoCode() {
        allowRoles(Role.EMPLOYEE);

        Response response = given()
                .accept(ContentType.JSON)
                .get("/monthend/{month}/status-overview/employee", "2026-13");

        ProblemDto problem = problemOf(response, 400);
        assertThat(problem.getCode()).isNull();
        assertThat(problem.getViolations())
                .extracting(ViolationDto::getField, ViolationDto::getIn)
                .containsExactly(tuple("month", ViolationDto.InEnum.PATH));
    }

    @Test
    @TestSecurity(user = "test")
    void beanValidation_shouldBeProblemWithBodyViolationAndNoCode() {
        allowRoles(Role.PROJECT_LEAD);

        Response response = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body("{}")
                .put("/projects/" + PROJECT_ID.value() + "/leistungsnachweis-enabled");

        ProblemDto problem = problemOf(response, 400);
        assertThat(problem.getCode()).isNull();
        assertThat(problem.getViolations())
                .extracting(ViolationDto::getField, ViolationDto::getIn)
                .containsExactly(tuple("enabled", ViolationDto.InEnum.BODY));
    }

    @Test
    @TestSecurity(user = "test")
    void malformedBodyValue_shouldBeProblemWithoutCodeOrViolations() {
        allowRoles(Role.PROJECT_LEAD);

        Response response = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body("{\"enabled\": \"maybe\"}")
                .put("/projects/" + PROJECT_ID.value() + "/leistungsnachweis-enabled");

        ProblemDto problem = problemOf(response, 400);
        assertThat(problem.getCode()).isNull();
        assertThat(problem.getViolations()).isEmpty();
    }

    @Test
    void unauthenticated_shouldBeProblemWithoutCode() {
        Response response = given()
                .accept(ContentType.JSON)
                .get("/projects/settings");

        ProblemDto problem = problemOf(response, 401);
        assertThat(problem.getTitle()).isEqualTo("Unauthorized");
        assertThat(problem.getCode()).isNull();
    }

    @Test
    @TestSecurity(user = "test")
    void missingRole_shouldBeProblemWithForbiddenCode() {
        allowRoles(Role.EMPLOYEE);

        Response response = given()
                .accept(ContentType.JSON)
                .get("/projects/settings");

        ProblemDto problem = problemOf(response, 403);
        assertThat(problem.getTitle()).isEqualTo("Forbidden");
        assertThat(problem.getCode()).isEqualTo("FORBIDDEN");
    }

    @Test
    @TestSecurity(user = "test")
    void unexpectedFailure_shouldBeProblemWithoutDetailOrCode() {
        allowRoles(Role.PROJECT_LEAD);
        when(getProjectSettingsUseCase.getLeadProjects(ACTOR_ID))
                .thenThrow(new IllegalStateException("SELECT * FROM secret_table"));

        Response response = given()
                .accept(ContentType.JSON)
                .get("/projects/settings");

        ProblemDto problem = problemOf(response, 500);
        assertThat(problem.getTitle()).isEqualTo("Internal Server Error");
        assertThat(problem.getDetail()).isNull();
        assertThat(problem.getCode()).isNull();
        assertThat(response.asString()).doesNotContain("secret_table");
    }

    @Test
    @TestSecurity(user = "test")
    void success_shouldKeepJsonMediaType() {
        allowRoles(Role.PROJECT_LEAD);
        when(getProjectSettingsUseCase.getLeadProjects(ACTOR_ID)).thenReturn(List.of());

        given()
                .accept(ContentType.JSON)
                .get("/projects/settings")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    private void allowRoles(Role... roles) {
        when(authenticatedActorContext.roles()).thenReturn(Set.of(roles));
    }

    private static ProblemDto problemOf(Response response, int status) {
        response.then()
                .statusCode(status)
                .contentType(PROBLEM_JSON);
        ProblemDto problem = response.as(ProblemDto.class);
        assertThat(problem.getStatus()).isEqualTo(status);
        return problem;
    }
}
