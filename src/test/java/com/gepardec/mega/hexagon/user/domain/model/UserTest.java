package com.gepardec.mega.hexagon.user.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void grantProjectLeadRole_shouldAddRoleWhenMissing() {
        User user = user(Set.of(Role.EMPLOYEE));

        User updatedUser = user.grantProjectLeadRole();

        assertThat(updatedUser.roles()).containsExactlyInAnyOrder(Role.EMPLOYEE, Role.PROJECT_LEAD);
    }

    @Test
    void grantProjectLeadRole_shouldBeIdempotentWhenRoleAlreadyPresent() {
        User user = user(Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));

        User updatedUser = user.grantProjectLeadRole();

        assertThat(updatedUser).isEqualTo(user);
    }

    @Test
    void revokeProjectLeadRole_shouldRemoveRoleWhenPresent() {
        User user = user(Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));

        User updatedUser = user.revokeProjectLeadRole();

        assertThat(updatedUser.roles()).containsExactly(Role.EMPLOYEE);
    }

    @Test
    void revokeProjectLeadRole_shouldBeIdempotentWhenRoleAlreadyMissing() {
        User user = user(Set.of(Role.EMPLOYEE));

        User updatedUser = user.revokeProjectLeadRole();

        assertThat(updatedUser).isEqualTo(user);
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
