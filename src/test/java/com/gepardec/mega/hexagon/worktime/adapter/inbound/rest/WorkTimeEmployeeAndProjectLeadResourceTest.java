package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ApiError;
import com.gepardec.mega.hexagon.generated.model.WorkTimeReportResponse;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetEmployeeWorkTimeUseCase;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetProjectLeadWorkTimeUseCase;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeUserNotFoundException;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEmployee;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEntry;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeProject;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
class WorkTimeEmployeeAndProjectLeadResourceTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 3);
    private static final UserId EMPLOYEE_ID = UserId.of(Instancio.create(UUID.class));
    private static final UserId PROJECT_LEAD_ID = UserId.of(Instancio.create(UUID.class));
    private static final ProjectId PROJECT_ID = ProjectId.of(Instancio.create(UUID.class));

    @InjectMock
    AuthenticatedActorContext authenticatedActorContext;

    @InjectMock
    GetEmployeeWorkTimeUseCase getEmployeeWorkTimeUseCase;

    @InjectMock
    GetProjectLeadWorkTimeUseCase getProjectLeadWorkTimeUseCase;

    @BeforeEach
    void setUp() {
        when(authenticatedActorContext.userId()).thenReturn(EMPLOYEE_ID);
    }

    @Test
    void getEmployeeWorkTimeReport_shouldReturnMappedReportForEmployeeRole() {
        allowRoles(Role.EMPLOYEE);
        WorkTimeReport report = new WorkTimeReport(
                MONTH,
                List.of(new WorkTimeEntry(
                        new WorkTimeEmployee(EMPLOYEE_ID, "Ada Lovelace"),
                        new WorkTimeProject(PROJECT_ID, "Spec First"),
                        12.5d,
                        1.5d,
                        20.0d
                ))
        );
        when(getEmployeeWorkTimeUseCase.getWorkTime(EMPLOYEE_ID, MONTH)).thenReturn(report);

        WorkTimeReportResponse response = given()
                .accept(ContentType.JSON)
                .get("/worktime/employee/" + MONTH)
                .then()
                .statusCode(200)
                .extract()
                .as(WorkTimeReportResponse.class);

        assertThat(response.getPayrollMonth()).isEqualTo(MONTH.toString());
        assertThat(response.getEntries()).singleElement().satisfies(entry -> {
            assertThat(entry.getEmployee().getId()).isEqualTo(EMPLOYEE_ID.value());
            assertThat(entry.getProject().getId()).isEqualTo(PROJECT_ID.value());
            assertThat(entry.getEmployeeMonthTotalHours()).isEqualTo(20.0d);
        });
        verify(getEmployeeWorkTimeUseCase).getWorkTime(EMPLOYEE_ID, MONTH);
    }

    @Test
    void getProjectLeadWorkTimeReport_shouldReturnMappedReportForLeadRole() {
        allowRoles(Role.PROJECT_LEAD);
        when(authenticatedActorContext.userId()).thenReturn(PROJECT_LEAD_ID);
        WorkTimeReport report = new WorkTimeReport(
                MONTH,
                List.of(new WorkTimeEntry(
                        new WorkTimeEmployee(EMPLOYEE_ID, "Ada Lovelace"),
                        new WorkTimeProject(PROJECT_ID, "Spec First"),
                        8.0d,
                        2.0d,
                        20.0d
                ))
        );
        when(getProjectLeadWorkTimeUseCase.getWorkTime(PROJECT_LEAD_ID, MONTH)).thenReturn(report);

        WorkTimeReportResponse response = given()
                .accept(ContentType.JSON)
                .get("/worktime/projects/" + MONTH)
                .then()
                .statusCode(200)
                .extract()
                .as(WorkTimeReportResponse.class);

        assertThat(response.getPayrollMonth()).isEqualTo(MONTH.toString());
        assertThat(response.getEntries()).singleElement().satisfies(entry -> {
            assertThat(entry.getEmployee().getId()).isEqualTo(EMPLOYEE_ID.value());
            assertThat(entry.getProject().getId()).isEqualTo(PROJECT_ID.value());
            assertThat(entry.getBillableHours()).isEqualTo(8.0d);
            assertThat(entry.getNonBillableHours()).isEqualTo(2.0d);
        });
        verify(getProjectLeadWorkTimeUseCase).getWorkTime(PROJECT_LEAD_ID, MONTH);
    }

    @Test
    void getEmployeeWorkTimeReport_shouldRejectProjectLeadRole() {
        allowRoles(Role.PROJECT_LEAD);

        given()
                .accept(ContentType.JSON)
                .get("/worktime/employee/" + MONTH)
                .then()
                .statusCode(403);

        verifyNoInteractions(getEmployeeWorkTimeUseCase);
    }

    @Test
    void getProjectLeadWorkTimeReport_shouldRejectEmployeeRole() {
        allowRoles(Role.EMPLOYEE);

        given()
                .accept(ContentType.JSON)
                .get("/worktime/projects/" + MONTH)
                .then()
                .statusCode(403);

        verifyNoInteractions(getProjectLeadWorkTimeUseCase);
    }

    @Test
    void getEmployeeWorkTimeReport_shouldMapDomainNotFoundExceptionTo404() {
        allowRoles(Role.EMPLOYEE);
        doThrow(new WorkTimeUserNotFoundException("user not found: " + EMPLOYEE_ID.value()))
                .when(getEmployeeWorkTimeUseCase).getWorkTime(EMPLOYEE_ID, MONTH);

        ApiError response = given()
                .accept(ContentType.JSON)
                .get("/worktime/employee/" + MONTH)
                .then()
                .statusCode(404)
                .extract()
                .as(ApiError.class);

        assertThat(response.getMessage()).isEqualTo("user not found: " + EMPLOYEE_ID.value());
    }

    private void allowRoles(Role... roles) {
        when(authenticatedActorContext.roles()).thenReturn(Set.of(roles));
    }
}
