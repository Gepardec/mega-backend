package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.CreateEmployeeClarificationRequest;
import com.gepardec.mega.hexagon.generated.model.CreateProjectLeadClarificationRequest;
import com.gepardec.mega.hexagon.generated.model.MonthEndOverviewClarificationEntry;
import com.gepardec.mega.hexagon.generated.model.MonthEndPreparationResponse;
import com.gepardec.mega.hexagon.generated.model.MonthEndStatusOverviewResponse;
import com.gepardec.mega.hexagon.generated.model.PrepareMonthEndProjectRequest;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetEmployeeMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetProjectLeadMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.PrematureMonthEndPreparationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndPreparationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
class MonthEndEmployeeAndProjectLeadResourceTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 3);
    private static final ProjectId PROJECT_ID = ProjectId.of(Instancio.create(UUID.class));
    private static final String PROJECT_NAME = "Test Project";
    private static final UserId EMPLOYEE_ID = UserId.of(Instancio.create(UUID.class));
    private static final UserId PROJECT_LEAD_ID = UserId.of(Instancio.create(UUID.class));
    private static final Instant CREATED_AT = Instant.parse("2026-03-20T08:15:00Z");

    @InjectMock
    AuthenticatedActorContext authenticatedActorContext;

    @InjectMock
    GetEmployeeMonthEndStatusOverviewUseCase getEmployeeMonthEndStatusOverviewUseCase;

    @InjectMock
    PrematureMonthEndPreparationUseCase prematureMonthEndPreparationUseCase;

    @InjectMock
    CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase;

    @InjectMock
    GetProjectLeadMonthEndStatusOverviewUseCase getProjectLeadMonthEndStatusOverviewUseCase;

    @InjectMock
    MonthEndUserSnapshotPort userSnapshotPort;

    @BeforeEach
    void setUp() {
        when(authenticatedActorContext.userId()).thenReturn(EMPLOYEE_ID);
        when(userSnapshotPort.findByIds(any(), any())).thenReturn(List.of(employeeRef(), projectLeadRef()));
    }

    @Test
    void prepareEmployeeMonthEndProject_shouldReturnPreparedTasksAndClarification() {
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
                .post("/monthend/employee/preparations")
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
    void createEmployeeMonthEndClarification_shouldReturnCreatedClarification() {
        allowRoles(Role.EMPLOYEE);
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
                "Need support."
        )).thenReturn(clarification);

        MonthEndOverviewClarificationEntry response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .post("/monthend/employee/clarifications")
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
    }


    @Test
    void createProjectLeadMonthEndClarification_shouldReturnCreatedClarification() {
        allowRoles(Role.PROJECT_LEAD);
        when(authenticatedActorContext.userId()).thenReturn(PROJECT_LEAD_ID);
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
                "Please fix the evidence."
        )).thenReturn(clarification);

        MonthEndOverviewClarificationEntry response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .post("/monthend/project-lead/clarifications")
                .then()
                .statusCode(201)
                .extract()
                .as(MonthEndOverviewClarificationEntry.class);

        assertThat(response.getProjectId()).isEqualTo(PROJECT_ID.value());
        assertThat(response.getCreatedBy().getId()).isEqualTo(PROJECT_LEAD_ID.value());
        assertThat(response.getSubjectEmployee().getId()).isEqualTo(EMPLOYEE_ID.value());
        assertThat(response.getText()).isEqualTo("Please fix the evidence.");
        assertThat(response.getCanEditText()).isTrue();
        assertThat(response.getCanDelete()).isTrue();
        assertThat(response.getCanResolve()).isFalse();
    }

    @Test
    void getEmployeeMonthEndStatusOverview_shouldReturnMappedOverviewForEmployeeRole() {
        allowRoles(Role.EMPLOYEE);
        MonthEndClarification clarification = projectLeadClarification("Please revisit the supporting evidence.");
        MonthEndStatusOverview overview = new MonthEndStatusOverview(
                EMPLOYEE_ID,
                MONTH,
                List.of(new MonthEndStatusOverviewItem(
                        MonthEndTaskId.of(Instancio.create(UUID.class)),
                        MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        MonthEndTaskStatus.OPEN,
                        projectRef(),
                        employeeRef(),
                        true,
                        null
                )),
                List.of(clarification)
        );
        when(getEmployeeMonthEndStatusOverviewUseCase.getOverview(EMPLOYEE_ID, MONTH)).thenReturn(overview);

        MonthEndStatusOverviewResponse response = given()
                .accept(ContentType.JSON)
                .queryParam("month", MONTH.toString())
                .get("/monthend/employee/status-overview")
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndStatusOverviewResponse.class);

        assertThat(response.getMonth()).isEqualTo(MONTH.toString());
        assertThat(response.getEntries()).singleElement().satisfies(entry -> {
            assertThat(entry.getProject().getId()).isEqualTo(PROJECT_ID.value());
            assertThat(entry.getSubjectEmployee().getId()).isEqualTo(EMPLOYEE_ID.value());
            assertThat(entry.getCanComplete()).isTrue();
        });
        assertThat(response.getClarifications()).singleElement().satisfies(c -> {
            assertThat(c.getSubjectEmployee().getId()).isEqualTo(EMPLOYEE_ID.value());
            assertThat(c.getCreatedBy().getId()).isEqualTo(PROJECT_LEAD_ID.value());
            assertThat(c.getResolvedBy()).isNull();
            assertThat(c.getCanResolve()).isTrue();
            assertThat(c.getCanEditText()).isFalse();
            assertThat(c.getCanDelete()).isFalse();
        });
        verify(getEmployeeMonthEndStatusOverviewUseCase).getOverview(EMPLOYEE_ID, MONTH);
    }

    @Test
    void getEmployeeMonthEndStatusOverview_shouldRejectProjectLeadRole() {
        allowRoles(Role.PROJECT_LEAD);

        given()
                .accept(ContentType.JSON)
                .queryParam("month", MONTH.toString())
                .get("/monthend/employee/status-overview")
                .then()
                .statusCode(403);

        verifyNoInteractions(getEmployeeMonthEndStatusOverviewUseCase);
    }

    @Test
    void getProjectLeadMonthEndStatusOverview_shouldReturnMappedOverviewForLeadRole() {
        allowRoles(Role.PROJECT_LEAD);
        when(authenticatedActorContext.userId()).thenReturn(PROJECT_LEAD_ID);
        MonthEndClarification resolvedClarification = employeeClarification("Everything is clarified.")
                .resolve(PROJECT_LEAD_ID, "Confirmed by project lead.", CREATED_AT.plusSeconds(600));
        MonthEndStatusOverview overview = new MonthEndStatusOverview(
                PROJECT_LEAD_ID,
                MONTH,
                List.of(new MonthEndStatusOverviewItem(
                        MonthEndTaskId.of(Instancio.create(UUID.class)),
                        MonthEndTaskType.ABRECHNUNG,
                        MonthEndTaskStatus.OPEN,
                        projectRef(),
                        null,
                        true,
                        null
                )),
                List.of(resolvedClarification)
        );
        when(getProjectLeadMonthEndStatusOverviewUseCase.getOverview(PROJECT_LEAD_ID, MONTH)).thenReturn(overview);

        MonthEndStatusOverviewResponse response = given()
                .accept(ContentType.JSON)
                .queryParam("month", MONTH.toString())
                .get("/monthend/project-lead/status-overview")
                .then()
                .statusCode(200)
                .extract()
                .as(MonthEndStatusOverviewResponse.class);

        assertThat(response.getMonth()).isEqualTo(MONTH.toString());
        assertThat(response.getEntries()).singleElement().satisfies(entry -> {
            assertThat(entry.getProject().getId()).isEqualTo(PROJECT_ID.value());
            assertThat(entry.getSubjectEmployee()).isNull();
            assertThat(entry.getCanComplete()).isTrue();
        });
        assertThat(response.getClarifications()).singleElement().satisfies(c -> {
            assertThat(c.getSubjectEmployee().getId()).isEqualTo(EMPLOYEE_ID.value());
            assertThat(c.getCreatedBy().getId()).isEqualTo(EMPLOYEE_ID.value());
            assertThat(c.getResolvedBy().getId()).isEqualTo(PROJECT_LEAD_ID.value());
            assertThat(c.getCanResolve()).isFalse();
            assertThat(c.getCanEditText()).isFalse();
            assertThat(c.getCanDelete()).isFalse();
        });
        verify(getProjectLeadMonthEndStatusOverviewUseCase).getOverview(PROJECT_LEAD_ID, MONTH);
    }

    @Test
    void getProjectLeadMonthEndStatusOverview_shouldRejectEmployeeRole() {
        allowRoles(Role.EMPLOYEE);

        given()
                .accept(ContentType.JSON)
                .queryParam("month", MONTH.toString())
                .get("/monthend/project-lead/status-overview")
                .then()
                .statusCode(403);

        verifyNoInteractions(getProjectLeadMonthEndStatusOverviewUseCase);
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
                MonthEndClarificationId.of(Instancio.create(UUID.class)),
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
                MonthEndClarificationId.of(Instancio.create(UUID.class)),
                MONTH,
                PROJECT_ID,
                EMPLOYEE_ID,
                PROJECT_LEAD_ID,
                Set.of(PROJECT_LEAD_ID),
                text,
                CREATED_AT
        );
    }

    private ProjectRef projectRef() {
        return new ProjectRef(PROJECT_ID, 77, PROJECT_NAME);
    }

    private UserRef employeeRef() {
        return new UserRef(EMPLOYEE_ID, FullName.of("Test", "Employee"), ZepUsername.of("test.employee"));
    }

    private UserRef projectLeadRef() {
        return new UserRef(PROJECT_LEAD_ID, FullName.of("Test", "Project Lead"), ZepUsername.of("test.projectlead"));
    }
}
