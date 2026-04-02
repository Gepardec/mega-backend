package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectAssignmentPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.UserStatus;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResolveMonthEndEmployeeProjectContextServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId employeeId = UserId.of(Instancio.create(UUID.class));
    private final UserId activeLeadId = UserId.of(Instancio.create(UUID.class));
    private final UserId inactiveLeadId = UserId.of(Instancio.create(UUID.class));

    private MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private MonthEndUserSnapshotPort monthEndUserSnapshotPort;
    private MonthEndProjectAssignmentPort monthEndProjectAssignmentPort;
    private ResolveMonthEndEmployeeProjectContextService service;

    @BeforeEach
    void setUp() {
        monthEndProjectSnapshotPort = mock(MonthEndProjectSnapshotPort.class);
        monthEndUserSnapshotPort = mock(MonthEndUserSnapshotPort.class);
        monthEndProjectAssignmentPort = mock(MonthEndProjectAssignmentPort.class);
        service = new ResolveMonthEndEmployeeProjectContextService(
                monthEndProjectSnapshotPort,
                monthEndUserSnapshotPort,
                monthEndProjectAssignmentPort
        );
    }

    @Test
    void resolve_shouldReturnContextWithOnlyActiveProjectLeads_whenProjectAndEmployeeContextAreValid() {
        MonthEndProjectSnapshot project = activeProject(Set.of(activeLeadId, inactiveLeadId));
        MonthEndUserSnapshot employee = activeUser(employeeId, "employee");
        MonthEndUserSnapshot activeLead = activeUser(activeLeadId, "lead-active");
        MonthEndUserSnapshot inactiveLead = inactiveUser(inactiveLeadId, "lead-inactive");

        when(monthEndProjectSnapshotPort.findAll()).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findAll()).thenReturn(List.of(employee, activeLead, inactiveLead));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(project.zepId(), month))
                .thenReturn(Set.of(employee.zepUsername()));

        MonthEndEmployeeProjectContext result = service.resolve(month, projectId, employeeId);

        assertThat(result.project()).isEqualTo(project);
        assertThat(result.subjectEmployee()).isEqualTo(employee);
        assertThat(result.eligibleProjectLeadIds()).containsExactly(activeLeadId);
    }

    @Test
    void resolve_shouldThrow_whenProjectIsMissingOrInactiveInMonth() {
        MonthEndProjectSnapshot inactiveProject = new MonthEndProjectSnapshot(
                projectId,
                77,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2026, 2, 28),
                true,
                Set.of(activeLeadId)
        );
        when(monthEndProjectSnapshotPort.findAll()).thenReturn(List.of(inactiveProject));

        assertThatThrownBy(() -> service.resolve(month, projectId, employeeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("project context not found");
    }

    @Test
    void resolve_shouldThrow_whenSubjectEmployeeIsNotActiveInMonth() {
        MonthEndProjectSnapshot project = activeProject(Set.of(activeLeadId));
        MonthEndUserSnapshot inactiveEmployee = inactiveUser(employeeId, "employee");

        when(monthEndProjectSnapshotPort.findAll()).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findAll()).thenReturn(List.of(inactiveEmployee));

        assertThatThrownBy(() -> service.resolve(month, projectId, employeeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("employee context not found");
    }

    @Test
    void resolve_shouldThrow_whenEmployeeIsNotAssignedToProject() {
        MonthEndProjectSnapshot project = activeProject(Set.of(activeLeadId));
        MonthEndUserSnapshot employee = activeUser(employeeId, "employee");
        MonthEndUserSnapshot activeLead = activeUser(activeLeadId, "lead-active");

        when(monthEndProjectSnapshotPort.findAll()).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findAll()).thenReturn(List.of(employee, activeLead));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(project.zepId(), month))
                .thenReturn(Set.of("someone-else"));

        assertThatThrownBy(() -> service.resolve(month, projectId, employeeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not assigned");
    }

    private MonthEndProjectSnapshot activeProject(Set<UserId> leadIds) {
        return new MonthEndProjectSnapshot(
                projectId,
                77,
                LocalDate.of(2025, 1, 1),
                null,
                true,
                leadIds
        );
    }

    private MonthEndUserSnapshot activeUser(UserId userId, String username) {
        return new MonthEndUserSnapshot(
                userId,
                username,
                UserStatus.ACTIVE,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2020, 1, 1), null))
        );
    }

    private MonthEndUserSnapshot inactiveUser(UserId userId, String username) {
        return new MonthEndUserSnapshot(
                userId,
                username,
                UserStatus.INACTIVE,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2020, 1, 1), null))
        );
    }
}
