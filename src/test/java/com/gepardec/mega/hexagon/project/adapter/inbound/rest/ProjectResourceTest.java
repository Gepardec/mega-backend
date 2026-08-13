package com.gepardec.mega.hexagon.project.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.LeistungsnachweisToggleRequestDto;
import com.gepardec.mega.hexagon.project.application.port.inbound.GetLeadProjectsUseCase;
import com.gepardec.mega.hexagon.project.application.port.inbound.SetLeistungsnachweisEnabledUseCase;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.ForbiddenException;
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
import static org.mockito.Mockito.*;

@QuarkusTest
@TestSecurity(user = "test")
class ProjectResourceTest {


    @InjectMock
    GetLeadProjectsUseCase getLeadProjectsUseCase;
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

    @Test
    void getLeadProjects_shouldReturnMappedProjects_whenUserIsLead() {
        allowRoles(Role.PROJECT_LEAD);
        Project project = new Project(PROJECT_ID,123,"X", LocalDate.now(),null,true, true, Set.of(LEAD_ID));
        when(getLeadProjectsUseCase.getLeadProjects(LEAD_ID)).thenReturn(List.of(project));

        given()
                .accept(ContentType.JSON)
                .get("/projects")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("[0].id",is(PROJECT_ID.value().toString()))
                .body("[0].name", is("X"));

    }

    @Test
    void getLeadProjects_shouldReturnEmptyList_whenLeadUserHasNoProjects() {
        allowRoles(Role.PROJECT_LEAD);
        when(getLeadProjectsUseCase.getLeadProjects(LEAD_ID)).thenReturn(List.of());

        given()
                .accept(ContentType.JSON)
                .get("/projects")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", is(0));
    }

    @Test
    void getLeadProjects_shouldRejectWithForbidden_whenUserIsNotLead() {
        allowRoles(Role.EMPLOYEE);

        given()
                .accept(ContentType.JSON)
                .get("/projects")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void setLeistungsnachweisEnabled_shoudlToggleOwnProject() {
        allowRoles(Role.PROJECT_LEAD);
        var request = new LeistungsnachweisToggleRequestDto().enabled(false);

        given()
                .accept(ContentType.JSON)
                .body(request)
                .contentType(ContentType.JSON)
                .put("/projects/" + PROJECT_ID.value() + "/leistungsnachweis-enabled")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        verify(setLeistungsnachweisEnabledUseCase).setLeistungsnachweisEnabled(PROJECT_ID, LEAD_ID, false);
    }

    @Test
    void setLeistungsnachweisEnabled_shouldReturnForbidden_whenUserIsNotLead() {
        allowRoles(Role.PROJECT_LEAD);
        var request = new LeistungsnachweisToggleRequestDto().enabled(false);
        doThrow(new ForbiddenException("user is not lead"))
                .when(setLeistungsnachweisEnabledUseCase)
                .setLeistungsnachweisEnabled(PROJECT_ID, LEAD_ID, false);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(request)
                .put("/projects/" + PROJECT_ID.value() + "/leistungsnachweis-enabled")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);

    }

    @Test
    void setLeistungsnachweisEnabled_shoudlReturnNotFound_whenProjectIsUnknown() {
        allowRoles(Role.PROJECT_LEAD);
        var request = new LeistungsnachweisToggleRequestDto().enabled(false);
        doThrow(new IllegalArgumentException("Project not found"))
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
}
