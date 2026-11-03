package com.gepardec.mega.hexagon.user.domain.services;

import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.model.AdminEmails;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.OfficeManagementEmails;
import com.gepardec.mega.hexagon.user.domain.model.ProjectCoordinatorEmails;
import com.gepardec.mega.hexagon.user.domain.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserRolePolicyServiceTest {

    @Test
    void determineRoles_shouldReturnEmployeeOnlyForRegularUser() {
        UserRolePolicyService service = new UserRolePolicyService(new OfficeManagementEmails(Set.of("om@example.com")), new AdminEmails(Set.of("admin@example.com")), new ProjectCoordinatorEmails(Set.of("coordinator@example.com")));

        Set<Role> roles = service.determineRoles("employee@example.com", null);

        assertThat(roles).containsExactly(Role.EMPLOYEE);
    }

    @Test
    void determineRoles_shouldAddOfficeManagementRoleWhenEmailMatchesConfiguredSet() {
        UserRolePolicyService service = new UserRolePolicyService(new OfficeManagementEmails(Set.of("om@example.com")), new AdminEmails(Set.of("admin@example.com")), new ProjectCoordinatorEmails(Set.of("coordinator@example.com")));

        Set<Role> roles = service.determineRoles("  OM@example.com  ", null);

        assertThat(roles).containsExactlyInAnyOrder(Role.EMPLOYEE, Role.OFFICE_MANAGEMENT);
    }

    @Test
    void determineRoles_shouldAddAdminRoleWhenEmailMatchesConfiguredSet() {
        UserRolePolicyService service = new UserRolePolicyService(new OfficeManagementEmails(Set.of("om@example.com")), new AdminEmails(Set.of(" Admin@Example.com ")), new ProjectCoordinatorEmails(Set.of("coordinator@example.com")));

        Set<Role> roles = service.determineRoles("admin@example.com", null);

        assertThat(roles).containsExactlyInAnyOrder(Role.EMPLOYEE, Role.ADMIN);
    }

    @Test
    void determineRoles_shouldRevokeAdminRoleWhenEmailLeftAdminList() {
        UserRolePolicyService service = new UserRolePolicyService(new OfficeManagementEmails(Set.of("om@example.com")), new AdminEmails(Set.of()), new ProjectCoordinatorEmails(Set.of("coordinator@example.com")));

        Set<Role> roles = service.determineRoles(
                "admin@example.com",
                user(Set.of(Role.EMPLOYEE, Role.ADMIN))
        );

        assertThat(roles).containsExactly(Role.EMPLOYEE);
    }

    @Test
    void determineRoles_shouldPreserveProjectLeadRoleForExistingUser() {
        UserRolePolicyService service = new UserRolePolicyService(new OfficeManagementEmails(Set.of("om@example.com")), new AdminEmails(Set.of("admin@example.com")), new ProjectCoordinatorEmails(Set.of("coordinator@example.com")));

        Set<Role> roles = service.determineRoles(
                "employee@example.com",
                user(Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD))
        );

        assertThat(roles).containsExactlyInAnyOrder(Role.EMPLOYEE, Role.PROJECT_LEAD);
    }

    @Test
    void updateProjectLeadRoleIfNeeded_shouldGrantProjectLeadRoleWhenRequested() {
        UserRolePolicyService service = new UserRolePolicyService(new OfficeManagementEmails(Set.of("om@example.com")), new AdminEmails(Set.of("admin@example.com")), new ProjectCoordinatorEmails(Set.of("coordinator@example.com")));

        Optional<User> updatedUser = service.updateProjectLeadRoleIfNeeded(user(Set.of(Role.EMPLOYEE)), true);

        assertThat(updatedUser)
                .hasValueSatisfying(user -> assertThat(user.roles()).containsExactlyInAnyOrder(Role.EMPLOYEE, Role.PROJECT_LEAD));
    }

    @Test
    void updateProjectLeadRoleIfNeeded_shouldRevokeProjectLeadRoleWhenNotRequested() {
        UserRolePolicyService service = new UserRolePolicyService(new OfficeManagementEmails(Set.of("om@example.com")), new AdminEmails(Set.of("admin@example.com")), new ProjectCoordinatorEmails(Set.of("coordinator@example.com")));

        Optional<User> updatedUser = service.updateProjectLeadRoleIfNeeded(user(Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD)), false);

        assertThat(updatedUser)
                .hasValueSatisfying(user -> assertThat(user.roles()).containsExactly(Role.EMPLOYEE));
    }

    @Test
    void updateProjectLeadRoleIfNeeded_shouldReturnEmptyWhenRoleAlreadyMatchesAssignment() {
        UserRolePolicyService service = new UserRolePolicyService(new OfficeManagementEmails(Set.of("om@example.com")), new AdminEmails(Set.of("admin@example.com")), new ProjectCoordinatorEmails(Set.of("coordinator@example.com")));

        Optional<User> updatedUser = service.updateProjectLeadRoleIfNeeded(user(Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD)), true);

        assertThat(updatedUser).isEmpty();
    }

    @Test
    void determineRoles_shouldAddCoordinatorRoleWithoutImplyingProjectLead() {
        UserRolePolicyService service = serviceWithCoordinators(Set.of(" Coordinator@Example.com "));

        Set<Role> roles = service.determineRoles("coordinator@example.com", null);

        assertThat(roles).containsExactlyInAnyOrder(Role.EMPLOYEE, Role.PROJECT_COORDINATOR);
    }

    @Test
    void determineRoles_shouldRevokeCoordinatorRoleWhenEmailLeftCoordinatorList() {
        UserRolePolicyService service = serviceWithCoordinators(Set.of());

        Set<Role> roles = service.determineRoles(
                "coordinator@example.com",
                user(Set.of(Role.EMPLOYEE, Role.PROJECT_COORDINATOR))
        );

        assertThat(roles).containsExactly(Role.EMPLOYEE);
    }

    @Test
    void determineRoles_shouldKeepProjectLeadAlongsideCoordinatorRole() {
        UserRolePolicyService service = serviceWithCoordinators(Set.of("coordinator@example.com"));

        Set<Role> roles = service.determineRoles(
                "coordinator@example.com",
                user(Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD))
        );

        assertThat(roles).containsExactlyInAnyOrder(Role.EMPLOYEE, Role.PROJECT_LEAD, Role.PROJECT_COORDINATOR);
    }

    private UserRolePolicyService serviceWithCoordinators(Set<String> coordinatorEmails) {
        return new UserRolePolicyService(
                new OfficeManagementEmails(Set.of("om@example.com")),
                new AdminEmails(Set.of("admin@example.com")),
                new ProjectCoordinatorEmails(coordinatorEmails)
        );
    }

    private User user(Set<Role> roles) {
        return new User(
                UserId.generate(),
                Email.of("employee@example.com"),
                FullName.of("Employee", "User"),
                ZepUsername.of("employee"),
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.now().minusYears(1), null)),
                roles
        );
    }
}
