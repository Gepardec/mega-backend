package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.application.port.inbound.SyncProjectLeadRolesResult;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.OfficeManagementEmails;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.user.domain.services.UserRolePolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SyncProjectLeadRolesServiceTest {

    private UserRepository userRepository;
    private SyncProjectLeadRolesService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new SyncProjectLeadRolesService(
                userRepository,
                new UserRolePolicyService(new OfficeManagementEmails(Set.of()))
        );
    }

    @Test
    void sync_shouldPersistUsersWhoGainProjectLeadRole() {
        UserId leadId = UserId.generate();
        when(userRepository.findAll()).thenReturn(List.of(user(leadId, "lead", Set.of(Role.EMPLOYEE))));

        SyncProjectLeadRolesResult result = service.sync(Set.of(leadId));

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users).singleElement()
                    .satisfies(updatedUser -> assertThat(updatedUser.roles()).containsExactlyInAnyOrder(Role.EMPLOYEE, Role.PROJECT_LEAD));
            return true;
        }));
        assertThat(result.rolesAdded()).isEqualTo(1);
        assertThat(result.rolesRevoked()).isZero();
    }

    @Test
    void sync_shouldPersistUsersWhoLoseProjectLeadRole() {
        UserId formerLeadId = UserId.generate();
        when(userRepository.findAll()).thenReturn(List.of(user(formerLeadId, "former-lead", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD))));

        SyncProjectLeadRolesResult result = service.sync(Set.of());

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users).singleElement()
                    .satisfies(updatedUser -> assertThat(updatedUser.roles()).containsExactly(Role.EMPLOYEE));
            return true;
        }));
        assertThat(result.rolesAdded()).isZero();
        assertThat(result.rolesRevoked()).isEqualTo(1);
    }

    @Test
    void sync_shouldNotPersistWhenRolesAlreadyMatchLeadAssignments() {
        UserId leadId = UserId.generate();
        when(userRepository.findAll()).thenReturn(List.of(
                user(leadId, "lead", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD)),
                user(UserId.generate(), "employee", Set.of(Role.EMPLOYEE))
        ));

        SyncProjectLeadRolesResult result = service.sync(Set.of(leadId));

        verify(userRepository, never()).saveAll(anyList());
        assertThat(result.rolesAdded()).isZero();
        assertThat(result.rolesRevoked()).isZero();
    }

    private User user(UserId userId, String username, Set<Role> roles) {
        return new User(
                userId,
                Email.of(username + "@example.com"),
                FullName.of("Test", "User"),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.now().minusYears(1), null)),
                roles
        );
    }
}
