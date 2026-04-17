package com.gepardec.mega.hexagon.monthend.domain.services;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndProjectContextNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
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

class MonthEndProjectContextServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId activeLeadId = UserId.of(Instancio.create(UUID.class));
    private final UserId inactiveLeadId = UserId.of(Instancio.create(UUID.class));

    private MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private MonthEndUserSnapshotPort monthEndUserSnapshotPort;
    private MonthEndProjectContextService service;

    @BeforeEach
    void setUp() {
        monthEndProjectSnapshotPort = mock(MonthEndProjectSnapshotPort.class);
        monthEndUserSnapshotPort = mock(MonthEndUserSnapshotPort.class);
        service = new MonthEndProjectContextService(monthEndProjectSnapshotPort, monthEndUserSnapshotPort);
    }

    @Test
    void resolve_shouldReturnContextWithOnlyActiveLeads_whenProjectExists() {
        MonthEndProjectSnapshot project = activeProject(Set.of(activeLeadId, inactiveLeadId));
        UserRef activeLead = activeUser(activeLeadId, "lead-active");

        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of(activeLead));

        MonthEndProjectContext result = service.resolve(month, projectId);

        assertThat(result.month()).isEqualTo(month);
        assertThat(result.project()).isEqualTo(project);
        assertThat(result.eligibleProjectLeadIds()).containsExactly(activeLeadId);
    }

    @Test
    void resolve_shouldExcludeInactiveLeadsFromEligibleLeads() {
        MonthEndProjectSnapshot project = activeProject(Set.of(activeLeadId, inactiveLeadId));
        UserRef activeLead = activeUser(activeLeadId, "lead-active");

        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of(activeLead));

        MonthEndProjectContext result = service.resolve(month, projectId);

        assertThat(result.eligibleProjectLeadIds())
                .isNotEmpty()
                .doesNotContain(inactiveLeadId);
    }

    @Test
    void resolve_shouldThrow_whenProjectIsNotActiveInMonth() {
        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of());

        assertThatThrownBy(() -> service.resolve(month, projectId))
                .isInstanceOf(MonthEndProjectContextNotFoundException.class)
                .hasMessageContaining("project context not found");
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
