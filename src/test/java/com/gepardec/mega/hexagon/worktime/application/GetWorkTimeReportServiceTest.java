package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.shared.domain.model.Capacity;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeProjectSnapshotPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeUserSnapshotPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeZepPort;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeErrorCode;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeException;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import io.smallrye.mutiny.Uni;
import org.assertj.core.api.ThrowableAssert;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWorkTimeReportServiceTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 3);

    @Mock
    private WorkTimeProjectSnapshotPort workTimeProjectSnapshotPort;

    @Mock
    private WorkTimeUserSnapshotPort workTimeUserSnapshotPort;

    @Mock
    private WorkTimeZepPort workTimeZepPort;

    private GetWorkTimeReportService service;

    @BeforeEach
    void setUp() {
        service = new GetWorkTimeReportService(
                workTimeProjectSnapshotPort,
                workTimeUserSnapshotPort,
                workTimeZepPort
        );
    }

    @Nested
    class EmployeeCapacity {

        @Test
        void getWorkTime_shouldAggregateHoursByProjectAndPreserveMonthTotal() {
            UserId employeeId = UserId.of(Instancio.create(UUID.class));
            ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
            ProjectId secondProjectId = ProjectId.of(Instancio.create(UUID.class));
            when(workTimeUserSnapshotPort.findById(employeeId, MONTH)).thenReturn(Optional.of(user(employeeId, "ada", "Ada Lovelace")));
            when(workTimeZepPort.fetchAttendancesForEmployee("ada", MONTH)).thenReturn(Uni.createFrom().item(List.of(
                    new WorkTimeAttendance("ada", 11, 2.5d, 0.0d),
                    new WorkTimeAttendance("ada", 11, 0.0d, 1.5d),
                    new WorkTimeAttendance("ada", 22, 3.0d, 0.0d)
            )));
            when(workTimeProjectSnapshotPort.findByZepId(11, MONTH)).thenReturn(Optional.of(project(projectId, 11, "Alpha")));
            when(workTimeProjectSnapshotPort.findByZepId(22, MONTH)).thenReturn(Optional.of(project(secondProjectId, 22, "Beta")));

            WorkTimeReport report = service.getWorkTime(employeeId, MONTH, Capacity.EMPLOYEE);

            assertThat(report.payrollMonth()).isEqualTo(MONTH);
            assertThat(report.entries()).extracting(entry -> entry.project().id())
                    .containsExactly(projectId, secondProjectId);
            assertThat(report.entries()).anySatisfy(entry -> {
                assertThat(entry.project().id()).isEqualTo(projectId);
                assertThat(entry.billableHours()).isEqualTo(2.5d);
                assertThat(entry.nonBillableHours()).isEqualTo(1.5d);
                assertThat(entry.employeeMonthTotalHours()).isEqualTo(7.0d);
            });
            assertThat(report.entries()).anySatisfy(entry -> {
                assertThat(entry.project().id()).isEqualTo(secondProjectId);
                assertThat(entry.billableHours()).isEqualTo(3.0d);
                assertThat(entry.nonBillableHours()).isZero();
                assertThat(entry.employeeMonthTotalHours()).isEqualTo(7.0d);
            });
        }

        @Test
        void getWorkTime_shouldSkipBookingsOnUnknownProjects() {
            UserId employeeId = UserId.of(Instancio.create(UUID.class));
            ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
            when(workTimeUserSnapshotPort.findById(employeeId, MONTH)).thenReturn(Optional.of(user(employeeId, "ada", "Ada Lovelace")));
            when(workTimeZepPort.fetchAttendancesForEmployee("ada", MONTH)).thenReturn(Uni.createFrom().item(List.of(
                    new WorkTimeAttendance("ada", 11, 2.0d, 0.0d),
                    new WorkTimeAttendance("ada", 99, 4.0d, 0.0d)
            )));
            when(workTimeProjectSnapshotPort.findByZepId(11, MONTH)).thenReturn(Optional.of(project(projectId, 11, "Alpha")));
            when(workTimeProjectSnapshotPort.findByZepId(99, MONTH)).thenReturn(Optional.empty());

            WorkTimeReport report = service.getWorkTime(employeeId, MONTH, Capacity.EMPLOYEE);

            assertThat(report.entries()).singleElement().satisfies(entry -> {
                assertThat(entry.project().id()).isEqualTo(projectId);
                assertThat(entry.employeeMonthTotalHours()).isEqualTo(6.0d);
            });
        }

        @Test
        void getWorkTime_shouldReturnEmptyReportWhenNoAttendancesExist() {
            UserId employeeId = UserId.of(Instancio.create(UUID.class));
            when(workTimeUserSnapshotPort.findById(employeeId, MONTH)).thenReturn(Optional.of(user(employeeId, "ada", "Ada Lovelace")));
            when(workTimeZepPort.fetchAttendancesForEmployee("ada", MONTH)).thenReturn(Uni.createFrom().item(List.of()));

            WorkTimeReport report = service.getWorkTime(employeeId, MONTH, Capacity.EMPLOYEE);

            assertThat(report.payrollMonth()).isEqualTo(MONTH);
            assertThat(report.entries()).isEmpty();
        }

        @Test
        void getWorkTime_shouldThrowWorkTimeUserNotFoundExceptionWhenUserIsMissing() {
            UserId employeeId = UserId.of(Instancio.create(UUID.class));
            when(workTimeUserSnapshotPort.findById(employeeId, MONTH)).thenReturn(Optional.empty());

            ThrowableAssert.ThrowingCallable throwingCallable = () -> service.getWorkTime(employeeId, MONTH, Capacity.EMPLOYEE);

            assertThatThrownBy(throwingCallable)
                    .isInstanceOfSatisfying(WorkTimeException.class,
                        thrown -> assertThat(thrown.errorCode()).isEqualTo(WorkTimeErrorCode.USER_NOT_FOUND))
                    .hasMessage("user not found: " + employeeId.value());
        }

        @Test
        void getWorkTime_shouldFailAsDefectWhenZepUsernameIsMissing() {
            UserId employeeId = UserId.of(Instancio.create(UUID.class));
            when(workTimeUserSnapshotPort.findById(employeeId, MONTH)).thenReturn(Optional.of(userWithoutZepUsername(employeeId)));

            ThrowableAssert.ThrowingCallable throwingCallable = () -> service.getWorkTime(employeeId, MONTH, Capacity.EMPLOYEE);

            assertThatThrownBy(throwingCallable)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("zep username missing for user: " + employeeId.value());
        }
    }

    @Nested
    class ControllingCapacity {

        @Test
        void getWorkTime_shouldAggregateEntriesAcrossAllLeadProjectsAndPreserveEmployeeTotals() {
            UserId projectLeadId = UserId.of(Instancio.create(UUID.class));
            UserId employeeId = UserId.of(Instancio.create(UUID.class));
            UserId secondEmployeeId = UserId.of(Instancio.create(UUID.class));
            ProjectRef alpha = project(ProjectId.of(Instancio.create(UUID.class)), 11, "Alpha");
            ProjectRef beta = project(ProjectId.of(Instancio.create(UUID.class)), 22, "Beta");
            when(workTimeProjectSnapshotPort.findAllByLead(projectLeadId, MONTH)).thenReturn(List.of(alpha, beta));
            when(workTimeZepPort.fetchProjectMembershipForMonth(11, MONTH)).thenReturn(Uni.createFrom().item(List.of("ada", "grace")));
            when(workTimeZepPort.fetchProjectMembershipForMonth(22, MONTH)).thenReturn(Uni.createFrom().item(List.of("ada")));
            when(workTimeZepPort.fetchAttendancesForEmployee("ada", MONTH)).thenReturn(Uni.createFrom().item(List.of(
                    new WorkTimeAttendance("ada", 11, 5.0d, 0.0d),
                    new WorkTimeAttendance("ada", 22, 1.5d, 0.5d),
                    new WorkTimeAttendance("ada", 33, 2.0d, 0.0d)
            )));
            when(workTimeZepPort.fetchAttendancesForEmployee("grace", MONTH)).thenReturn(Uni.createFrom().item(List.of(
                    new WorkTimeAttendance("grace", 11, 0.0d, 3.5d)
            )));
            when(workTimeUserSnapshotPort.findByZepUsernames(Set.of(ZepUsername.of("ada"), ZepUsername.of("grace")), MONTH)).thenReturn(List.of(
                    user(employeeId, "ada", "Ada Lovelace"),
                    user(secondEmployeeId, "grace", "Grace Hopper")
            ));

            WorkTimeReport report = service.getWorkTime(projectLeadId, MONTH, Capacity.CONTROLLING);

            assertThat(report.payrollMonth()).isEqualTo(MONTH);
            // The booking on non-lead project 33 is filtered out, but still counts towards the employee total.
            assertThat(report.entries()).extracting(entry -> entry.employee().id(), entry -> entry.project().id())
                    .containsExactly(
                            tuple(employeeId, alpha.id()),
                            tuple(employeeId, beta.id()),
                            tuple(secondEmployeeId, alpha.id())
                    );
            assertThat(report.entries()).anySatisfy(entry -> {
                assertThat(entry.employee().id()).isEqualTo(employeeId);
                assertThat(entry.project().id()).isEqualTo(alpha.id());
                assertThat(entry.billableHours()).isEqualTo(5.0d);
                assertThat(entry.nonBillableHours()).isZero();
                assertThat(entry.employeeMonthTotalHours()).isEqualTo(9.0d);
            });
            assertThat(report.entries()).anySatisfy(entry -> {
                assertThat(entry.employee().id()).isEqualTo(employeeId);
                assertThat(entry.project().id()).isEqualTo(beta.id());
                assertThat(entry.billableHours()).isEqualTo(1.5d);
                assertThat(entry.nonBillableHours()).isEqualTo(0.5d);
                assertThat(entry.employeeMonthTotalHours()).isEqualTo(9.0d);
            });
            assertThat(report.entries()).anySatisfy(entry -> {
                assertThat(entry.employee().id()).isEqualTo(secondEmployeeId);
                assertThat(entry.project().id()).isEqualTo(alpha.id());
                assertThat(entry.billableHours()).isZero();
                assertThat(entry.nonBillableHours()).isEqualTo(3.5d);
                assertThat(entry.employeeMonthTotalHours()).isEqualTo(3.5d);
            });
            verify(workTimeZepPort).fetchProjectMembershipForMonth(11, MONTH);
            verify(workTimeZepPort).fetchProjectMembershipForMonth(22, MONTH);
        }

        @Test
        void getWorkTime_shouldReturnEmptyReportWhenCallerLeadsNoProjects() {
            UserId projectLeadId = UserId.of(Instancio.create(UUID.class));
            when(workTimeProjectSnapshotPort.findAllByLead(projectLeadId, MONTH)).thenReturn(List.of());

            WorkTimeReport report = service.getWorkTime(projectLeadId, MONTH, Capacity.CONTROLLING);

            assertThat(report.payrollMonth()).isEqualTo(MONTH);
            assertThat(report.entries()).isEmpty();
        }

        @Test
        void getWorkTime_shouldReturnEmptyReportWhenNoEmployeesBookedTime() {
            UserId projectLeadId = UserId.of(Instancio.create(UUID.class));
            ProjectRef alpha = project(ProjectId.of(Instancio.create(UUID.class)), 11, "Alpha");
            when(workTimeProjectSnapshotPort.findAllByLead(projectLeadId, MONTH)).thenReturn(List.of(alpha));
            when(workTimeZepPort.fetchProjectMembershipForMonth(11, MONTH)).thenReturn(Uni.createFrom().item(List.of()));

            WorkTimeReport report = service.getWorkTime(projectLeadId, MONTH, Capacity.CONTROLLING);

            assertThat(report.payrollMonth()).isEqualTo(MONTH);
            assertThat(report.entries()).isEmpty();
        }
    }

    @Test
    void getWorkTime_shouldReturnLeadEntryOnlyForLedProjectInControllingCapacityWithIdenticalHours() {
        UserId leadId = UserId.of(Instancio.create(UUID.class));
        UserRef lead = user(leadId, "ada", "Ada Lovelace");
        ProjectRef led = project(ProjectId.of(Instancio.create(UUID.class)), 11, "Led");
        ProjectRef notLed = project(ProjectId.of(Instancio.create(UUID.class)), 22, "Not led");
        List<WorkTimeAttendance> attendances = List.of(
                new WorkTimeAttendance("ada", 11, 4.0d, 1.0d),
                new WorkTimeAttendance("ada", 22, 2.0d, 0.0d)
        );
        when(workTimeUserSnapshotPort.findById(leadId, MONTH)).thenReturn(Optional.of(lead));
        when(workTimeProjectSnapshotPort.findByZepId(11, MONTH)).thenReturn(Optional.of(led));
        when(workTimeProjectSnapshotPort.findByZepId(22, MONTH)).thenReturn(Optional.of(notLed));
        when(workTimeProjectSnapshotPort.findAllByLead(leadId, MONTH)).thenReturn(List.of(led));
        when(workTimeZepPort.fetchProjectMembershipForMonth(11, MONTH)).thenReturn(Uni.createFrom().item(List.of("ada")));
        when(workTimeUserSnapshotPort.findByZepUsernames(Set.of(ZepUsername.of("ada")), MONTH)).thenReturn(List.of(lead));
        when(workTimeZepPort.fetchAttendancesForEmployee("ada", MONTH)).thenReturn(Uni.createFrom().item(attendances));

        WorkTimeReport employeeReport = service.getWorkTime(leadId, MONTH, Capacity.EMPLOYEE);
        WorkTimeReport controllingReport = service.getWorkTime(leadId, MONTH, Capacity.CONTROLLING);

        assertThat(employeeReport.entries()).extracting(entry -> entry.project().id())
                .containsExactly(led.id(), notLed.id());
        assertThat(controllingReport.entries()).singleElement().satisfies(entry -> {
            assertThat(entry.project().id()).isEqualTo(led.id());
            assertThat(entry.billableHours()).isEqualTo(4.0d);
            assertThat(entry.nonBillableHours()).isOne();
            assertThat(entry.employeeMonthTotalHours()).isEqualTo(7.0d);
        });
        assertThat(employeeReport.entries()).filteredOn(entry -> entry.project().id().equals(led.id()))
                .singleElement()
                .isEqualTo(controllingReport.entries().getFirst());
    }

    private UserRef user(UserId userId, String username, String fullName) {
        return new UserRef(userId, toFullName(fullName), ZepUsername.of(username));
    }

    private ProjectRef project(ProjectId projectId, int zepId, String name) {
        return new ProjectRef(projectId, zepId, name);
    }

    private UserRef userWithoutZepUsername(UserId userId) {
        return new UserRef(userId, FullName.of("Ada", "Lovelace"), ZepUsername.of(""));
    }

    private FullName toFullName(String fullName) {
        String[] parts = fullName.split(" ", 2);
        return FullName.of(parts[0], parts.length > 1 ? parts[1] : null);
    }
}
