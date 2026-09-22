package com.gepardec.mega.hexagon.project.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.LeistungsnachweisToggleRequestDto;
import com.gepardec.mega.hexagon.project.application.port.inbound.GetProjectSettingsUseCase;
import com.gepardec.mega.hexagon.project.application.port.inbound.SetLeistungsnachweisEnabledUseCase;
import com.gepardec.mega.hexagon.project.domain.error.ProjectErrorCode;
import com.gepardec.mega.hexagon.project.domain.error.ProjectException;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
class ProjectResourceTest {


    @InjectMock
    GetProjectSettingsUseCase getProjectSettingsUseCase;
    @InjectMock
    SetLeistungsnachweisEnabledUseCase setLeistungsnachweisEnabledUseCase;
    @InjectMock
    AuthenticatedActorContext authenticatedActorContext;


    private static final UserId LEAD_ID = UserId.of(UUID.randomUUID());
    private static final ProjectId PROJECT_ID = ProjectId.of(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        when(authenticatedActorContext.userId()).thenReturn(LEAD_ID);
    }

    private void allowRoles(Role... roles) {
        when(authenticatedActorContext.roles()).thenReturn(Set.of(roles));
    }

    private Project project(boolean billable, boolean leistungsnachweisEnabled) {
        return new Project(PROJECT_ID, 123, "X", LocalDate.now(), null, billable, leistungsnachweisEnabled, Set.of(LEAD_ID));
    }

    @Test
    void getProjectSettings_shouldReturnMappedProjects_whenUserIsCoordinator() {
        allowRoles(Role.PROJECT_COORDINATOR);
        Project project = new Project(PROJECT_ID, 123, "X", LocalDate.now(), null, true, true, Set.of(LEAD_ID));
        when(getProjectSettingsUseCase.getSettings()).thenReturn(List.of(project));

        given()
                .accept(ContentType.JSON)
                .get("/projects/settings")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("[0].project.id", is(PROJECT_ID.value().toString()))
                .body("[0].project.name", is("X"))
                .body("[0].leistungsnachweisEnabled", is(true));

    }

    @Test
    void getProjectSettings_shouldReturnEmptyList_whenNoProjectQualifies() {
        allowRoles(Role.PROJECT_COORDINATOR);
        when(getProjectSettingsUseCase.getSettings()).thenReturn(List.of());

        given()
                .accept(ContentType.JSON)
                .get("/projects/settings")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", is(0));
    }

    @Test
    void getProjectSettings_shouldRejectWithForbidden_whenUserIsLeadWithoutCoordinatorRole() {
        allowRoles(Role.EMPLOYEE, Role.PROJECT_LEAD);

        given()
                .accept(ContentType.JSON)
                .get("/projects/settings")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        verifyNoInteractions(getProjectSettingsUseCase);
    }

    @Test
    void setLeistungsnachweisEnabled_shouldReturnForbidden_whenUserIsLeadWithoutCoordinatorRole() {
        allowRoles(Role.EMPLOYEE, Role.PROJECT_LEAD);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(new LeistungsnachweisToggleRequestDto().enabled(false))
                .put("/projects/" + PROJECT_ID.value() + "/leistungsnachweis-enabled")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        verifyNoInteractions(setLeistungsnachweisEnabledUseCase);
    }

    @Test
    void setLeistungsnachweisEnabled_shouldToggleProject() {
        allowRoles(Role.PROJECT_COORDINATOR);
        var request = new LeistungsnachweisToggleRequestDto().enabled(false);
        when(setLeistungsnachweisEnabledUseCase.setLeistungsnachweisEnabled(PROJECT_ID, LEAD_ID, false))
                .thenReturn(project(true, false));

        given()
                .accept(ContentType.JSON)
                .body(request)
                .contentType(ContentType.JSON)
                .put("/projects/" + PROJECT_ID.value() + "/leistungsnachweis-enabled")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("project.id", is(PROJECT_ID.value().toString()))
                .body("project.name", is("X"))
                .body("project.zepUrl", notNullValue())
                .body("leistungsnachweisEnabled", is(false));

        verify(setLeistungsnachweisEnabledUseCase).setLeistungsnachweisEnabled(PROJECT_ID, LEAD_ID, false);
    }

    @Test
    void setLeistungsnachweisEnabled_shouldReturnUpdatedSettings_whenEnabling() {
        allowRoles(Role.PROJECT_COORDINATOR);
        var request = new LeistungsnachweisToggleRequestDto().enabled(true);
        when(setLeistungsnachweisEnabledUseCase.setLeistungsnachweisEnabled(PROJECT_ID, LEAD_ID, true))
                .thenReturn(project(true, true));

        given()
                .accept(ContentType.JSON)
                .body(request)
                .contentType(ContentType.JSON)
                .put("/projects/" + PROJECT_ID.value() + "/leistungsnachweis-enabled")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("project.id", is(PROJECT_ID.value().toString()))
                .body("leistungsnachweisEnabled", is(true));
    }

    @Test
    void setLeistungsnachweisEnabled_shouldReturnDisabledSettings_whenProjectIsNonBillable() {
        allowRoles(Role.PROJECT_COORDINATOR);
        var request = new LeistungsnachweisToggleRequestDto().enabled(false);
        when(setLeistungsnachweisEnabledUseCase.setLeistungsnachweisEnabled(PROJECT_ID, LEAD_ID, false))
                .thenReturn(project(false, false));

        given()
                .accept(ContentType.JSON)
                .body(request)
                .contentType(ContentType.JSON)
                .put("/projects/" + PROJECT_ID.value() + "/leistungsnachweis-enabled")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("leistungsnachweisEnabled", is(false));
    }

    @Test
    void setLeistungsnachweisEnabled_shoudlReturnNotFound_whenProjectIsUnknown() {
        allowRoles(Role.PROJECT_COORDINATOR);
        var request = new LeistungsnachweisToggleRequestDto().enabled(false);
        doThrow(new ProjectException(ProjectErrorCode.NOT_FOUND, "Project not found"))
                .when(setLeistungsnachweisEnabledUseCase)
                .setLeistungsnachweisEnabled(PROJECT_ID, LEAD_ID, false);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(request)
                .put("/projects/" + PROJECT_ID.value() + "/leistungsnachweis-enabled")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void setLeistungsnachweisEnabled_shouldReturnBadRequest_whenEnabledIsNull() {
        allowRoles(Role.PROJECT_COORDINATOR);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(new LeistungsnachweisToggleRequestDto().enabled(null))
                .put("/projects/" + PROJECT_ID.value() + "/leistungsnachweis-enabled")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        verifyNoInteractions(setLeistungsnachweisEnabledUseCase);
    }

    @Test
    void setLeistungsnachweisEnabled_shouldReturnBadRequest_whenBodyIsEmpty() {
        allowRoles(Role.PROJECT_COORDINATOR);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body("{}")
                .put("/projects/" + PROJECT_ID.value() + "/leistungsnachweis-enabled")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        verifyNoInteractions(setLeistungsnachweisEnabledUseCase);
    }

    @Test
    void setLeistungsnachweisEnabled_shouldReturnBadRequest_whenUseCaseThrowsNotApplicable() {
        allowRoles(Role.PROJECT_COORDINATOR);
        var request = new LeistungsnachweisToggleRequestDto().enabled(true);
        doThrow(new ProjectException(ProjectErrorCode.LEISTUNGSNACHWEIS_NOT_APPLICABLE, "not applicable"))
                .when(setLeistungsnachweisEnabledUseCase)
                .setLeistungsnachweisEnabled(PROJECT_ID, LEAD_ID, true);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(request)
                .put("/projects/" + PROJECT_ID.value() + "/leistungsnachweis-enabled")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
}
