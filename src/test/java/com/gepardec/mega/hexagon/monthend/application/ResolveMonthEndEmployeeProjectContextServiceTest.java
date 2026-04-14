package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndEmployeeContextNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndEmployeeNotAssignedToProjectException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndProjectContextNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectAssignmentPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        UserRef employee = activeUser(employeeId, "employee");
        UserRef activeLead = activeUser(activeLeadId, "lead-active");

        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of(employee, activeLead));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(project.zepId(), month))
                .thenReturn(Set.of(employee.zepUsername().value()));

        MonthEndEmployeeProjectContext result = service.resolve(month, projectId, employeeId);

        assertThat(result.project()).isEqualTo(project);
        assertThat(result.subjectEmployee()).isEqualTo(employee);
        assertThat(result.eligibleProjectLeadIds()).containsExactly(activeLeadId);
    }

    @Test
    void resolve_shouldThrow_whenProjectIsMissingOrInactiveInMonth() {
        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of());

        assertThatThrownBy(() -> service.resolve(month, projectId, employeeId))
                .isInstanceOf(MonthEndProjectContextNotFoundException.class)
                .hasMessageContaining("project context not found");
    }

    @Test
    void resolve_shouldThrow_whenSubjectEmployeeIsNotActiveInMonth() {
        MonthEndProjectSnapshot project = activeProject(Set.of(activeLeadId));

        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of());

        assertThatThrownBy(() -> service.resolve(month, projectId, employeeId))
                .isInstanceOf(MonthEndEmployeeContextNotFoundException.class)
                .hasMessageContaining("employee context not found");
    }

    @Test
    void resolve_shouldThrow_whenEmployeeIsNotAssignedToProject() {
        MonthEndProjectSnapshot project = activeProject(Set.of(activeLeadId));
        UserRef employee = activeUser(employeeId, "employee");
        UserRef activeLead = activeUser(activeLeadId, "lead-active");

        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of(employee, activeLead));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(project.zepId(), month))
                .thenReturn(Set.of("someone-else"));

        assertThatThrownBy(() -> service.resolve(month, projectId, employeeId))
                .isInstanceOf(MonthEndEmployeeNotAssignedToProjectException.class)
                .hasMessageContaining("not assigned");
    }

    private MonthEndProjectSnapshot activeProject(Set<UserId> leadIds) {
        return new MonthEndProjectSnapshot(
                projectId,
                77,
                "Project-77",
                true,
                leadIds
        );
    }

    private UserRef activeUser(UserId userId, String username) {
        return new UserRef(
                userId,
                FullName.of(username, "User"),
                ZepUsername.of(username)
        );
    }
}
