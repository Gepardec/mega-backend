package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;
import com.gepardec.mega.hexagon.project.domain.port.inbound.ReconcileLeadsResult;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.project.domain.port.outbound.UserLookupPort;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ZepProjectPort;
import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.Role;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReconcileLeadsServiceTest {

    private ZepProjectPort zepProjectPort;
    private ProjectRepository projectRepository;
    private UserLookupPort userLookupPort;
    private UserRepository userRepository;
    private ReconcileLeadsService service;

    @BeforeEach
    void setUp() {
        zepProjectPort = mock(ZepProjectPort.class);
        projectRepository = mock(ProjectRepository.class);
        userLookupPort = mock(UserLookupPort.class);
        userRepository = mock(UserRepository.class);
        service = new ReconcileLeadsService(zepProjectPort, projectRepository, userLookupPort, userRepository);
    }

    private Project projectWithZepId(int zepId) {
        return Project.create(ProjectId.generate(), new ZepProjectProfile(zepId, "P" + zepId, LocalDate.now(), null, false));
    }

    private User userWithId(UUID id, String username, Set<Role> roles) {
        return new User(
                UserId.of(id),
                Email.of(username + "@test.com"),
                FullName.of("F", "L"),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.now().minusYears(1), null)),
                roles
        );
    }

    @Test
    void reconcile_resolvesLeadUsernameToUserId() {
        UUID leadId = UUID.randomUUID();
        Project project = projectWithZepId(1);

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(1)).thenReturn(List.of("jdoe"));
        when(userLookupPort.findUserIdByZepUsername(ZepUsername.of("jdoe"))).thenReturn(Optional.of(leadId));
        when(userRepository.findAll()).thenReturn(List.of());

        service.reconcile();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects.getFirst().getLeads()).containsExactly(leadId);
            return true;
        }));
    }

    @Test
    void reconcile_skipsUnknownUsername() {
        Project project = projectWithZepId(2);

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(2)).thenReturn(List.of("unknown"));
        when(userLookupPort.findUserIdByZepUsername(ZepUsername.of("unknown"))).thenReturn(Optional.empty());
        when(userRepository.findAll()).thenReturn(List.of());

        service.reconcile();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects.getFirst().getLeads()).isEmpty();
            return true;
        }));
    }

    @Test
    void reconcile_replacesFullLeadsSet() {
        UUID oldLead = UUID.randomUUID();
        UUID newLead = UUID.randomUUID();
        Project project = Project.reconstitute(ProjectId.generate(), 3, "P3", LocalDate.now(), null, false, Set.of(oldLead));

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(3)).thenReturn(List.of("newguy"));
        when(userLookupPort.findUserIdByZepUsername(ZepUsername.of("newguy"))).thenReturn(Optional.of(newLead));
        when(userRepository.findAll()).thenReturn(List.of());

        service.reconcile();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects.getFirst().getLeads()).containsExactly(newLead);
            assertThat(projects.getFirst().getLeads()).doesNotContain(oldLead);
            return true;
        }));
    }

    @Test
    void reconcile_assignsProjectLeadRole() {
        UUID leadId = UUID.randomUUID();
        Project project = projectWithZepId(4);
        User user = userWithId(leadId, "leaduser", EnumSet.of(Role.EMPLOYEE));

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(4)).thenReturn(List.of("leaduser"));
        when(userLookupPort.findUserIdByZepUsername(ZepUsername.of("leaduser"))).thenReturn(Optional.of(leadId));
        when(userRepository.findAll()).thenReturn(List.of(user));

        service.reconcile();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users.getFirst().roles()).contains(Role.PROJECT_LEAD);
            return true;
        }));
    }

    @Test
    void reconcile_revokesProjectLeadRoleWhenNoLongerLead() {
        UUID userId = UUID.randomUUID();
        Project project = projectWithZepId(5);
        User user = userWithId(userId, "exlead", EnumSet.of(Role.EMPLOYEE, Role.PROJECT_LEAD));

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(5)).thenReturn(List.of()); // no leads
        when(userRepository.findAll()).thenReturn(List.of(user));

        service.reconcile();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users.getFirst().roles()).doesNotContain(Role.PROJECT_LEAD);
            return true;
        }));
    }

    @Test
    void reconcile_doesNotCallUserSaveAllWhenNoRoleChanges() {
        UUID leadId = UUID.randomUUID();
        Project project = projectWithZepId(6);
        User user = userWithId(leadId, "stable", EnumSet.of(Role.EMPLOYEE, Role.PROJECT_LEAD));

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(6)).thenReturn(List.of("stable"));
        when(userLookupPort.findUserIdByZepUsername(ZepUsername.of("stable"))).thenReturn(Optional.of(leadId));
        when(userRepository.findAll()).thenReturn(List.of(user));

        service.reconcile();

        verify(userRepository, never()).saveAll(argThat(users -> !users.isEmpty()));
    }

    @Test
    void reconcile_continuesForRemainingLeadsAfterUnknownUsername() {
        UUID knownId = UUID.randomUUID();
        Project project = projectWithZepId(7);

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(7)).thenReturn(List.of("unknown", "known"));
        when(userLookupPort.findUserIdByZepUsername(ZepUsername.of("unknown"))).thenReturn(Optional.empty());
        when(userLookupPort.findUserIdByZepUsername(ZepUsername.of("known"))).thenReturn(Optional.of(knownId));
        when(userRepository.findAll()).thenReturn(List.of());

        service.reconcile();

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects.getFirst().getLeads()).containsExactly(knownId);
            return true;
        }));
    }

    @Test
    void reconcile_result_countsResolvedAndSkippedLeads() {
        UUID knownId = UUID.randomUUID();
        Project project = projectWithZepId(8);

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(8)).thenReturn(List.of("known", "unknown1", "unknown2"));
        when(userLookupPort.findUserIdByZepUsername(ZepUsername.of("known"))).thenReturn(Optional.of(knownId));
        when(userLookupPort.findUserIdByZepUsername(ZepUsername.of("unknown1"))).thenReturn(Optional.empty());
        when(userLookupPort.findUserIdByZepUsername(ZepUsername.of("unknown2"))).thenReturn(Optional.empty());
        when(userRepository.findAll()).thenReturn(List.of());

        ReconcileLeadsResult result = service.reconcile();

        assertThat(result.resolved()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(2);
    }

    @Test
    void reconcile_result_countsRolesAdded() {
        UUID leadId = UUID.randomUUID();
        Project project = projectWithZepId(9);
        User user = userWithId(leadId, "newlead", EnumSet.of(Role.EMPLOYEE));

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(9)).thenReturn(List.of("newlead"));
        when(userLookupPort.findUserIdByZepUsername(ZepUsername.of("newlead"))).thenReturn(Optional.of(leadId));
        when(userRepository.findAll()).thenReturn(List.of(user));

        ReconcileLeadsResult result = service.reconcile();

        assertThat(result.rolesAdded()).isEqualTo(1);
        assertThat(result.rolesRevoked()).isZero();
    }

    @Test
    void reconcile_result_countsRolesRevoked() {
        UUID userId = UUID.randomUUID();
        Project project = projectWithZepId(10);
        User user = userWithId(userId, "exlead", EnumSet.of(Role.EMPLOYEE, Role.PROJECT_LEAD));

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(zepProjectPort.fetchLeadUsernames(10)).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of(user));

        ReconcileLeadsResult result = service.reconcile();

        assertThat(result.rolesAdded()).isZero();
        assertThat(result.rolesRevoked()).isEqualTo(1);
    }
}
