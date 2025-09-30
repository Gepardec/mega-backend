package com.gepardec.mega.rest;

import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.EmploymentPeriod;
import com.gepardec.mega.domain.model.EmploymentPeriods;
import com.gepardec.mega.rest.api.SyncResource;
import com.gepardec.mega.rest.model.EmployeeDto;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.EnterpriseSyncService;
import com.gepardec.mega.service.api.PrematureEmployeeCheckSyncService;
import com.gepardec.mega.service.api.ProjectSyncService;
import com.gepardec.mega.service.api.StepEntryService;
import com.gepardec.mega.service.api.StepEntrySyncService;
import com.gepardec.mega.zep.ZepService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test", roles = "mega-cron:sync")
@OidcSecurity(claims = {
        @Claim(key = "email", value = "test@gepardec.com")
})
class SyncResourceTest {

    @InjectMock
    EmployeeService employeeService;

    @InjectMock
    ZepService zepService;

    @InjectMock
    StepEntryService stepEntryService;

    @Inject
    SyncResource syncResource;

    @Test
    void updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth_whenEmployeeHasNoTimesAndAllAbsences_thenSetStepStateDone() {
        Employee userUnderTest = createEmployeeForId("099-testUser", "test.user@gepardec.com", "2024-02-29");
        when(employeeService.getAllActiveEmployees())
                .thenReturn(
                        List.of(
                                createEmployeeForId("e02-testExternal", "external.user@gepardec.com", "2024-02-29"),
                                userUnderTest,
                                createEmployeeForId("100-testUser2", "test.user2@gepardec.com", "2024-02-29")
                        )
                );


        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        List<AbsenceTime> fehlzeitList = createAbsenceTimeListForUser(
                "099-testUser",
                new AbsenceEntry(previousMonth.atDay(1).toString(), previousMonth.atEndOfMonth().toString(), AbsenceType.PAID_SICK_LEAVE.getAbsenceName())
        );

        when(zepService.getAbsenceForEmployee(eq(userUnderTest), any(YearMonth.class)))
                .thenReturn(fehlzeitList);


        when(zepService.getEmployee(eq(userUnderTest.getUserId())))
                .thenReturn(userUnderTest);

        ArgumentCaptor<Employee> employeeArgumentCaptor = ArgumentCaptor.forClass(Employee.class);
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<YearMonth> payrollMonthCaptor = ArgumentCaptor.forClass(YearMonth.class);

        when(stepEntryService.setOpenAndAssignedStepEntriesDone(
                        employeeArgumentCaptor.capture(),
                        longArgumentCaptor.capture(),
                        payrollMonthCaptor.capture()
                )
        )
                .thenReturn(true);

        when(stepEntryService.findStepEntryForEmployeeAtStep(anyLong(), anyString(), anyString(), any(YearMonth.class)))
                .thenReturn(createStepEntry());

        List<EmployeeDto> actual = syncResource.updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth();

        assertThat(actual).isNotNull().size().isEqualTo(1);
        assertThat(employeeArgumentCaptor.getValue()).isEqualTo(userUnderTest);
        assertThat(longArgumentCaptor.getValue()).isEqualTo(1L);
        assertThat(payrollMonthCaptor.getValue()).isEqualTo(previousMonth);
        assertThat(actual.getFirst().getUserId()).isEqualTo(userUnderTest.getUserId());
    }


    @Test
    void updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth_whenEmployeeHasNoTimesAndAllAbsencesWithHomeOfficeAndVacation_thenReturnEmptyList() {
        Employee userUnderTest = createEmployeeForId("099-testUser", "test.user@gepardec.com", "2024-02-29");
        when(employeeService.getAllActiveEmployees())
                .thenReturn(
                        List.of(
                                createEmployeeForId("e02-externalUser", "external.user@gepardec.com", "2024-02-29"),
                                userUnderTest,
                                createEmployeeForId("100-testUser2", "test.user2@gepardec.com", "2024-02-29")
                        )
                );


        List<AbsenceTime> fehlzeitList = createAbsenceTimeListForUser(
                "099-testUser",
                new AbsenceEntry("2024-03-01", "2024-03-01", AbsenceType.PAID_SICK_LEAVE.getAbsenceName()),
                new AbsenceEntry("2024-03-04", "2024-03-08", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-11", "2024-03-15", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-18", "2024-03-22", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-25", "2024-03-29", AbsenceType.HOME_OFFICE_DAYS.getAbsenceName())
        );

        when(zepService.getAbsenceForEmployee(eq(userUnderTest), any(YearMonth.class)))
                .thenReturn(fehlzeitList);


        List<EmployeeDto> actual = syncResource.updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth();

        assertThat(actual).isEmpty();
    }


    @Test
    void updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth_whenEmployeeHasNoTimesAndSomeAbsences_thenReturnEmptyList() {
        Employee userUnderTest = createEmployeeForId("099-testUser", "test.user@gepardec.com", "2024-02-29");
        when(employeeService.getAllActiveEmployees())
                .thenReturn(
                        List.of(
                                createEmployeeForId("e02-externalUser", "external.user@gepardec.com", "2024-02-29"),
                                createEmployeeForId("100-testUser2", "test.user2@gepardec.com", "2024-02-29"),
                                userUnderTest
                        )
                );


        List<AbsenceTime> fehlzeitList = createAbsenceTimeListForUser(
                "099-testuser",
                new AbsenceEntry("2024-03-01", "2024-03-01", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-18", "2024-03-22", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-25", "2024-03-29", AbsenceType.HOME_OFFICE_DAYS.getAbsenceName())
        );

        Mockito.when(zepService.getAbsenceForEmployee(eq(userUnderTest), any(YearMonth.class)))
                .thenReturn(fehlzeitList);


        when(zepService.getEmployee(eq(userUnderTest.getUserId())))
                .thenReturn(createEmployeeForId("099-testUser", "test.user@gepardec.com", "2024-02-29"));

        List<EmployeeDto> actual = syncResource.updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth();

        assertThat(actual).isEmpty();
    }

    @Test
    void updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth_whenEmployeeIsExternal_thenReturnEmptyList() {
        Employee userUnderTest = createEmployeeForId("e02-externalUser", "external.user@gepardec.com", "2024-02-29");
        when(employeeService.getAllActiveEmployees())
                .thenReturn(
                        List.of(
                                userUnderTest,
                                createEmployeeForId("099-testUser", "test.user@gepardec.com", "2024-02-29"),
                                createEmployeeForId("100-testUser2", "test.user2@gepardec.com", "2024-02-29")
                        )
                );


        List<AbsenceTime> fehlzeitList = createAbsenceTimeListForUser(
                "e02-externalUser",
                new AbsenceEntry("2024-03-01", "2024-03-01", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-04", "2024-03-08", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-11", "2024-03-15", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-18", "2024-03-22", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-25", "2024-03-29", AbsenceType.VACATION_DAYS.getAbsenceName())
        );

        when(zepService.getAbsenceForEmployee(eq(userUnderTest), any(YearMonth.class)))
                .thenReturn(fehlzeitList);

        doNothing().when(zepService).updateEmployeesReleaseDate(anyString(), anyString());

        when(zepService.getEmployee(eq(userUnderTest.getUserId())))
                .thenReturn(createEmployeeForId("e02-externalUser", "external.user@gepardec.com", "2024-02-29"));

        List<EmployeeDto> actual = syncResource.updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth();

        assertThat(actual).isEmpty();
    }

    @Test
    void syncEmployees_returnsStatusOK() {
        try (Response response = syncResource.syncEmployees()) {
            assertThat(response.getStatus()).isEqualTo(200);
        }
    }

    static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of(null, YearMonth.of(2023, 6)),
                Arguments.of(YearMonth.of(2023, 6), null),
                Arguments.of(YearMonth.of(2023, 3), YearMonth.of(2023, 6))

        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void syncProjects(YearMonth from, YearMonth to) {
        ProjectSyncService projectSyncService = mock(ProjectSyncService.class);
        when(projectSyncService.generateProjects(any()))
                .thenReturn(true);

        try (Response response = syncResource.syncProjects(from, to)) {
            assertThat(Response.Status.OK.getStatusCode()).isEqualTo(response.getStatus());
        }
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void generateEnterpriseEntries(YearMonth from, YearMonth to) {
        EnterpriseSyncService enterpriseSyncService = mock(EnterpriseSyncService.class);
        when(enterpriseSyncService.generateEnterpriseEntries(any(YearMonth.class)))
                .thenReturn(true);

        try (Response response = syncResource.generateEnterpriseEntries(from, to)) {
            assertThat(Response.Status.OK.getStatusCode()).isEqualTo(response.getStatus());
        }
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void generateStepEntries(YearMonth from, YearMonth to) {
        StepEntrySyncService stepEntrySyncService = mock(StepEntrySyncService.class);
        when(stepEntrySyncService.generateStepEntries(any()))
                .thenReturn(true);

        try (Response response = syncResource.generateStepEntries(from, to)) {
            assertThat(Response.Status.OK.getStatusCode()).isEqualTo(response.getStatus());
        }
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void syncPrematureEmployeeChecks(YearMonth from, YearMonth to) {
        PrematureEmployeeCheckSyncService prematureEmployeeCheckSyncService = mock(PrematureEmployeeCheckSyncService.class);
        when(prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(any()))
                .thenReturn(true);

        try (Response response = syncResource.syncPrematureEmployeeChecks(from, to)) {
            assertThat(Response.Status.OK.getStatusCode()).isEqualTo(response.getStatus());
        }
    }

    //helpers
    private Employee createEmployeeForId(final String id, final String email, final String releaseDate) {
        return Employee.builder()
                .userId(id)
                .email(email)
                .releaseDate(releaseDate)
                .employmentPeriods(new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2020, 1, 1), null)))
                .build();
    }

    private static AbsenceTime createFehlzeitTypeForUser(final String userId, final String startDate, final String endDate, final String reason) {
        return new AbsenceTime(
                userId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                reason,
                true
        );
    }

    private static StepEntry createStepEntry() {
        StepEntry entry = new StepEntry();
        entry.setState(EmployeeState.OPEN);
        return entry;
    }

    //helper class for test classes above to reduce loc
    static class AbsenceEntry {
        String startDate;
        String endDate;
        String reason;

        public AbsenceEntry(String startDate, String endDate, String reason) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.reason = reason;
        }
    }

    private static List<AbsenceTime> createAbsenceTimeListForUser(String userId, AbsenceEntry... entries) {
        List<AbsenceTime> fehlzeitTypeList = new ArrayList<>();
        for (AbsenceEntry entry : entries) {
            fehlzeitTypeList.add(createFehlzeitTypeForUser(userId, entry.startDate, entry.endDate, entry.reason));
        }
        return fehlzeitTypeList;
    }
}
