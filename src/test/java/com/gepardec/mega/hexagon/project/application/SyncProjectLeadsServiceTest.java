package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;
import com.gepardec.mega.hexagon.project.domain.port.inbound.ProjectLeadSyncResult;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.project.domain.port.outbound.UserIdentityLookupPort;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ZepProjectPort;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumSet;
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
    private UserRepository userRepository;
    private SyncProjectLeadsService service;

    @BeforeEach
    void setUp() {
        zepProjectPort = mock(ZepProjectPort.class);
        projectRepository = mock(ProjectRepository.class);
        userIdentityLookupPort = mock(UserIdentityLookupPort.class);
        userRepository = mock(UserRepository.class);
        service = new SyncProjectLeadsService(zepProjectPort, projectRepository, userIdentityLookupPort, userRepository);
    }

    private Project projectWithZepId(int zepId) {
        return Project.create(ProjectId.generate(), new ZepProjectProfile(zepId, "P" + zepId, LocalDate.now(), null, false));
    }

    private User userWithId(UserId id, String username, Set<Role> roles) {
        return new User(
                id,
                Email.of(username + "@test.com"),
                FullName.of("F", "L"),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.now().minusYears(1), null)),
                roles
        );
    }

    @Test
    void sync_resolvesLeadUsernameToUserId() {
        UserId leadId = UserId.of(UUID.randomUUID());
        Project project = projectWithZepId(1);

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(1)).thenReturn(List.of("jdoe"));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("jdoe"))).thenReturn(Optional.of(leadId));
        when(userRepository.findAll()).thenReturn(List.of());

        service.sync();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects.getFirst().leads()).containsExactly(leadId);
            return true;
        }));
    }

    @Test
    void sync_skipsUnknownUsername() {
        Project project = projectWithZepId(2);

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(2)).thenReturn(List.of("unknown"));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("unknown"))).thenReturn(Optional.empty());
        when(userRepository.findAll()).thenReturn(List.of());

        ProjectLeadSyncResult result = service.sync();

        verify(projectRepository, never()).saveAll(anyList());
        assertThat(result.resolved()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
    }

    @Test
    void sync_replacesFullLeadsSet() {
        UserId oldLead = UserId.of(UUID.randomUUID());
        UserId newLead = UserId.of(UUID.randomUUID());
        Project project = new Project(ProjectId.generate(), 3, "P3", LocalDate.now(), null, false, Set.of(oldLead));

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(3)).thenReturn(List.of("newguy"));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("newguy"))).thenReturn(Optional.of(newLead));
        when(userRepository.findAll()).thenReturn(List.of());

        service.sync();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects.getFirst().leads()).containsExactly(newLead);
            assertThat(projects.getFirst().leads()).doesNotContain(oldLead);
            return true;
        }));
    }

    @Test
    void sync_assignsProjectLeadRole() {
        UserId leadId = UserId.of(UUID.randomUUID());
        Project project = projectWithZepId(4);
        User user = userWithId(leadId, "leaduser", EnumSet.of(Role.EMPLOYEE));

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(4)).thenReturn(List.of("leaduser"));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("leaduser"))).thenReturn(Optional.of(leadId));
        when(userRepository.findAll()).thenReturn(List.of(user));

        service.sync();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users.getFirst().roles()).contains(Role.PROJECT_LEAD);
            return true;
        }));
    }

    @Test
    void sync_revokesProjectLeadRoleWhenNoLongerLead() {
        UserId userId = UserId.of(UUID.randomUUID());
        Project project = projectWithZepId(5);
        User user = userWithId(userId, "exlead", EnumSet.of(Role.EMPLOYEE, Role.PROJECT_LEAD));

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(5)).thenReturn(List.of()); // no leads
        when(userRepository.findAll()).thenReturn(List.of(user));

        service.sync();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users.getFirst().roles()).doesNotContain(Role.PROJECT_LEAD);
            return true;
        }));
    }

    @Test
    void sync_doesNotPersistWhenLeadsAndRolesAreAlreadyUpToDate() {
        UserId leadId = UserId.of(UUID.randomUUID());
        Project project = new Project(ProjectId.generate(), 6, "P6", LocalDate.now(), null, false, Set.of(leadId));
        User user = userWithId(leadId, "stable", EnumSet.of(Role.EMPLOYEE, Role.PROJECT_LEAD));

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(6)).thenReturn(List.of("stable"));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("stable"))).thenReturn(Optional.of(leadId));
        when(userRepository.findAll()).thenReturn(List.of(user));

        service.sync();

        verify(projectRepository, never()).saveAll(anyList());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    void sync_continuesForRemainingLeadsAfterUnknownUsername() {
        UserId knownId = UserId.of(UUID.randomUUID());
        Project project = projectWithZepId(7);

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(7)).thenReturn(List.of("unknown", "known"));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("unknown"))).thenReturn(Optional.empty());
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("known"))).thenReturn(Optional.of(knownId));
        when(userRepository.findAll()).thenReturn(List.of());

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
        when(userRepository.findAll()).thenReturn(List.of());

        ProjectLeadSyncResult result = service.sync();

        assertThat(result.resolved()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(2);
    }

    @Test
    void sync_result_countsRolesAdded() {
        UserId leadId = UserId.of(UUID.randomUUID());
        Project project = projectWithZepId(9);
        User user = userWithId(leadId, "newlead", EnumSet.of(Role.EMPLOYEE));

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(9)).thenReturn(List.of("newlead"));
        when(userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of("newlead"))).thenReturn(Optional.of(leadId));
        when(userRepository.findAll()).thenReturn(List.of(user));

        ProjectLeadSyncResult result = service.sync();

        assertThat(result.rolesAdded()).isEqualTo(1);
        assertThat(result.rolesRevoked()).isZero();
    }

    @Test
    void sync_result_countsRolesRevoked() {
        UserId userId = UserId.of(UUID.randomUUID());
        Project project = projectWithZepId(10);
        User user = userWithId(userId, "exlead", EnumSet.of(Role.EMPLOYEE, Role.PROJECT_LEAD));

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(10)).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of(user));

        ProjectLeadSyncResult result = service.sync();

        assertThat(result.rolesAdded()).isZero();
        assertThat(result.rolesRevoked()).isEqualTo(1);
    }
}
