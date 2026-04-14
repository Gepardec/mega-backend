package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeProjectSnapshot;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeUserSnapshot;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeProjectSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeUserSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeZepPort;
import io.smallrye.mutiny.Uni;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private GetProjectLeadWorkTimeService service;

    @Test
    void getWorkTime_shouldAggregateEntriesAcrossAllLeadProjectsAndPreserveEmployeeTotals() {
        UserId projectLeadId = UserId.of(Instancio.create(UUID.class));
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        UserId secondEmployeeId = UserId.of(Instancio.create(UUID.class));
        WorkTimeProjectSnapshot alpha = project(ProjectId.of(Instancio.create(UUID.class)), 11, "Alpha");
        WorkTimeProjectSnapshot beta = project(ProjectId.of(Instancio.create(UUID.class)), 22, "Beta");
        when(workTimeProjectSnapshotPort.findAllByLead(projectLeadId)).thenReturn(List.of(alpha, beta));
        when(workTimeZepPort.fetchProjectMembershipForMonth(11, YearMonth.of(2026, 3))).thenReturn(Uni.createFrom().item(List.of("ada", "grace")));
        when(workTimeZepPort.fetchProjectMembershipForMonth(22, YearMonth.of(2026, 3))).thenReturn(Uni.createFrom().item(List.of("ada")));
        when(workTimeZepPort.fetchAttendancesForEmployee("ada", YearMonth.of(2026, 3))).thenReturn(Uni.createFrom().item(List.of(
                new WorkTimeAttendance("ada", 11, 5.0d, 0.0d),
                new WorkTimeAttendance("ada", 22, 1.5d, 0.5d),
                new WorkTimeAttendance("ada", 33, 2.0d, 0.0d)
        )));
        when(workTimeZepPort.fetchAttendancesForEmployee("grace", YearMonth.of(2026, 3))).thenReturn(Uni.createFrom().item(List.of(
                new WorkTimeAttendance("grace", 11, 0.0d, 3.5d)
        )));
        when(workTimeUserSnapshotPort.findByZepUsernames(Set.of(com.gepardec.mega.hexagon.user.domain.model.ZepUsername.of("ada"), com.gepardec.mega.hexagon.user.domain.model.ZepUsername.of("grace")))).thenReturn(List.of(
                user(employeeId, "ada", "Ada Lovelace"),
                user(secondEmployeeId, "grace", "Grace Hopper")
        ));

        WorkTimeReport report = service.getWorkTime(projectLeadId, YearMonth.of(2026, 3));

        assertThat(report.payrollMonth()).isEqualTo(YearMonth.of(2026, 3));
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
        verify(workTimeZepPort).fetchProjectMembershipForMonth(11, YearMonth.of(2026, 3));
        verify(workTimeZepPort).fetchProjectMembershipForMonth(22, YearMonth.of(2026, 3));
    }

    @Test
    void getWorkTime_shouldReturnEmptyReportWhenCallerLeadsNoProjects() {
        UserId projectLeadId = UserId.of(Instancio.create(UUID.class));
        when(workTimeProjectSnapshotPort.findAllByLead(projectLeadId)).thenReturn(List.of());

        WorkTimeReport report = service.getWorkTime(projectLeadId, YearMonth.of(2026, 3));

        assertThat(report.payrollMonth()).isEqualTo(YearMonth.of(2026, 3));
        assertThat(report.entries()).isEmpty();
    }

    @Test
    void getWorkTime_shouldReturnEmptyReportWhenNoEmployeesBookedTime() {
        UserId projectLeadId = UserId.of(Instancio.create(UUID.class));
        WorkTimeProjectSnapshot alpha = project(ProjectId.of(Instancio.create(UUID.class)), 11, "Alpha");
        when(workTimeProjectSnapshotPort.findAllByLead(projectLeadId)).thenReturn(List.of(alpha));
        when(workTimeZepPort.fetchProjectMembershipForMonth(11, YearMonth.of(2026, 3))).thenReturn(Uni.createFrom().item(List.of()));

        WorkTimeReport report = service.getWorkTime(projectLeadId, YearMonth.of(2026, 3));

        assertThat(report.payrollMonth()).isEqualTo(YearMonth.of(2026, 3));
        assertThat(report.entries()).isEmpty();
    }

    private WorkTimeUserSnapshot user(UserId userId, String username, String fullName) {
        return new WorkTimeUserSnapshot(userId, fullName, username);
    }

    private WorkTimeProjectSnapshot project(ProjectId projectId, int zepId, String name) {
        return new WorkTimeProjectSnapshot(projectId, zepId, name);
    }
}
