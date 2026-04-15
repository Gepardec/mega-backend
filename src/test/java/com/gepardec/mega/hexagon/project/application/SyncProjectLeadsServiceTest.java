package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.application.port.inbound.ProjectLeadSyncResult;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.project.domain.port.outbound.UserIdentityLookupPort;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ZepProjectPort;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SyncProjectLeadsServiceTest {

    private ZepProjectPort zepProjectPort;
    private ProjectRepository projectRepository;
    private UserIdentityLookupPort userIdentityLookupPort;
    private SyncProjectLeadsService service;

    @BeforeEach
    void setUp() {
        zepProjectPort = mock(ZepProjectPort.class);
        projectRepository = mock(ProjectRepository.class);
        userIdentityLookupPort = mock(UserIdentityLookupPort.class);
        service = new SyncProjectLeadsService(
                zepProjectPort,
                projectRepository,
                userIdentityLookupPort
        );
    }

    private Project projectWithZepId(int zepId) {
        return Project.create(ProjectId.generate(), new ZepProjectProfile(zepId, "P" + zepId, LocalDate.now(), null, false));
    }

    @Test
    void sync_resolvesLeadUsernameToUserId() {
        UserId leadId = UserId.of(UUID.randomUUID());
        Project project = projectWithZepId(1);

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(1)).thenReturn(List.of("jdoe"));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("jdoe"))).thenReturn(Optional.of(leadId));

        ProjectLeadSyncResult result = service.sync();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects.getFirst().leads()).containsExactly(leadId);
            return true;
        }));
        assertThat(result.leadUserIds()).containsExactly(leadId);
    }

    @Test
    void sync_skipsUnknownUsername() {
        Project project = projectWithZepId(2);

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(2)).thenReturn(List.of("unknown"));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("unknown"))).thenReturn(Optional.empty());

        ProjectLeadSyncResult result = service.sync();

        verify(projectRepository, never()).saveAll(anyList());
        assertThat(result.resolved()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.leadUserIds()).isEmpty();
    }

    @Test
    void sync_replacesFullLeadsSet() {
        UserId oldLead = UserId.of(UUID.randomUUID());
        UserId newLead = UserId.of(UUID.randomUUID());
        Project project = new Project(ProjectId.generate(), 3, "P3", LocalDate.now(), null, false, Set.of(oldLead));

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(3)).thenReturn(List.of("newguy"));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("newguy"))).thenReturn(Optional.of(newLead));

        service.sync();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects.getFirst().leads()).containsExactly(newLead);
            assertThat(projects.getFirst().leads()).doesNotContain(oldLead);
            return true;
        }));
    }

    @Test
    void sync_doesNotPersistWhenLeadsAndRolesAreAlreadyUpToDate() {
        UserId leadId = UserId.of(UUID.randomUUID());
        Project project = new Project(ProjectId.generate(), 6, "P6", LocalDate.now(), null, false, Set.of(leadId));

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(6)).thenReturn(List.of("stable"));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("stable"))).thenReturn(Optional.of(leadId));

        service.sync();

        verify(projectRepository, never()).saveAll(anyList());
    }

    @Test
    void sync_continuesForRemainingLeadsAfterUnknownUsername() {
        UserId knownId = UserId.of(UUID.randomUUID());
        Project project = projectWithZepId(7);

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(7)).thenReturn(List.of("unknown", "known"));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("unknown"))).thenReturn(Optional.empty());
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("known"))).thenReturn(Optional.of(knownId));

        service.sync();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects.getFirst().leads()).containsExactly(knownId);
            return true;
        }));
    }

    @Test
    void sync_result_countsResolvedAndSkippedLeads() {
        UserId knownId = UserId.of(UUID.randomUUID());
        Project project = projectWithZepId(8);

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(8)).thenReturn(List.of("known", "unknown1", "unknown2"));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("known"))).thenReturn(Optional.of(knownId));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("unknown1"))).thenReturn(Optional.empty());
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("unknown2"))).thenReturn(Optional.empty());

        ProjectLeadSyncResult result = service.sync();

        assertThat(result.resolved()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(2);
    }

    @Test
    void sync_returnsAllResolvedLeadUserIdsForFollowUpRoleAssignment() {
        UserId firstLeadId = UserId.of(UUID.randomUUID());
        UserId secondLeadId = UserId.of(UUID.randomUUID());
        Project firstProject = projectWithZepId(9);
        Project secondProject = projectWithZepId(10);

        when(projectRepository.findAll()).thenReturn(List.of(firstProject, secondProject));
        when(zepProjectPort.fetchLeadUsernames(9)).thenReturn(List.of("lead-a"));
        when(zepProjectPort.fetchLeadUsernames(10)).thenReturn(List.of("lead-b", "lead-a"));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("lead-a"))).thenReturn(Optional.of(firstLeadId));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("lead-b"))).thenReturn(Optional.of(secondLeadId));

        ProjectLeadSyncResult result = service.sync();

        assertThat(result.leadUserIds()).containsExactlyInAnyOrder(firstLeadId, secondLeadId);
    }
}
