package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeProjectSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeUserSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeZepPort;
import com.gepardec.mega.hexagon.worktime.domain.services.WorkTimeReportAssembler;
import io.smallrye.mutiny.Uni;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProjectLeadWorkTimeServiceTest {

    @Mock
    private WorkTimeProjectSnapshotPort workTimeProjectSnapshotPort;

    @Mock
    private WorkTimeUserSnapshotPort workTimeUserSnapshotPort;

    @Mock
    private WorkTimeZepPort workTimeZepPort;

    private GetProjectLeadWorkTimeService service;

    @BeforeEach
    void setUp() {
        service = new GetProjectLeadWorkTimeService(
                workTimeProjectSnapshotPort,
                workTimeUserSnapshotPort,
                workTimeZepPort,
                new WorkTimeReportAssembler()
        );
    }

    @Test
    void getWorkTime_shouldAggregateEntriesAcrossAllLeadProjectsAndPreserveEmployeeTotals() {
        YearMonth month = YearMonth.of(2026, 3);
        UserId projectLeadId = UserId.of(Instancio.create(UUID.class));
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        UserId secondEmployeeId = UserId.of(Instancio.create(UUID.class));
        ProjectRef alpha = project(ProjectId.of(Instancio.create(UUID.class)), 11, "Alpha");
        ProjectRef beta = project(ProjectId.of(Instancio.create(UUID.class)), 22, "Beta");
        when(workTimeProjectSnapshotPort.findAllByLead(projectLeadId, month)).thenReturn(List.of(alpha, beta));
        when(workTimeZepPort.fetchProjectMembershipForMonth(11, month)).thenReturn(Uni.createFrom().item(List.of("ada", "grace")));
        when(workTimeZepPort.fetchProjectMembershipForMonth(22, month)).thenReturn(Uni.createFrom().item(List.of("ada")));
        when(workTimeZepPort.fetchAttendancesForEmployee("ada", month)).thenReturn(Uni.createFrom().item(List.of(
                new WorkTimeAttendance("ada", 11, 5.0d, 0.0d),
                new WorkTimeAttendance("ada", 22, 1.5d, 0.5d),
                new WorkTimeAttendance("ada", 33, 2.0d, 0.0d)
        )));
        when(workTimeZepPort.fetchAttendancesForEmployee("grace", month)).thenReturn(Uni.createFrom().item(List.of(
                new WorkTimeAttendance("grace", 11, 0.0d, 3.5d)
        )));
        when(workTimeUserSnapshotPort.findByZepUsernames(Set.of(ZepUsername.of("ada"), ZepUsername.of("grace")), month)).thenReturn(List.of(
                user(employeeId, "ada", "Ada Lovelace"),
                user(secondEmployeeId, "grace", "Grace Hopper")
        ));

        WorkTimeReport report = service.getWorkTime(projectLeadId, month);

        assertThat(report.payrollMonth()).isEqualTo(month);
        assertThat(report.entries()).hasSize(3);
        assertThat(report.entries()).anySatisfy(entry -> {
            assertThat(entry.employee().id()).isEqualTo(employeeId);
            assertThat(entry.project().id()).isEqualTo(alpha.id());
            assertThat(entry.billableHours()).isEqualTo(5.0d);
            assertThat(entry.nonBillableHours()).isEqualTo(0.0d);
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
            assertThat(entry.billableHours()).isEqualTo(0.0d);
            assertThat(entry.nonBillableHours()).isEqualTo(3.5d);
            assertThat(entry.employeeMonthTotalHours()).isEqualTo(3.5d);
        });
        verify(workTimeZepPort).fetchProjectMembershipForMonth(11, month);
        verify(workTimeZepPort).fetchProjectMembershipForMonth(22, month);
    }

    @Test
    void getWorkTime_shouldReturnEmptyReportWhenCallerLeadsNoProjects() {
        UserId projectLeadId = UserId.of(Instancio.create(UUID.class));
        when(workTimeProjectSnapshotPort.findAllByLead(projectLeadId, YearMonth.of(2026, 3))).thenReturn(List.of());

        WorkTimeReport report = service.getWorkTime(projectLeadId, YearMonth.of(2026, 3));

        assertThat(report.payrollMonth()).isEqualTo(YearMonth.of(2026, 3));
        assertThat(report.entries()).isEmpty();
    }

    @Test
    void getWorkTime_shouldReturnEmptyReportWhenNoEmployeesBookedTime() {
        YearMonth month = YearMonth.of(2026, 3);
        UserId projectLeadId = UserId.of(Instancio.create(UUID.class));
        ProjectRef alpha = project(ProjectId.of(Instancio.create(UUID.class)), 11, "Alpha");
        when(workTimeProjectSnapshotPort.findAllByLead(projectLeadId, month)).thenReturn(List.of(alpha));
        when(workTimeZepPort.fetchProjectMembershipForMonth(11, month)).thenReturn(Uni.createFrom().item(List.of()));

        WorkTimeReport report = service.getWorkTime(projectLeadId, month);

        assertThat(report.payrollMonth()).isEqualTo(month);
        assertThat(report.entries()).isEmpty();
    }

    private UserRef user(UserId userId, String username, String fullName) {
        return new UserRef(userId, toFullName(fullName), ZepUsername.of(username));
    }

    private ProjectRef project(ProjectId projectId, int zepId, String name) {
        return new ProjectRef(projectId, zepId, name);
    }

    private FullName toFullName(String fullName) {
        String[] parts = fullName.split(" ", 2);
        return FullName.of(parts[0], parts.length > 1 ? parts[1] : null);
    }
}
