package com.gepardec.mega.hexagon.user.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void isExternal_shouldReturnTrueWhenZepUsernameStartsWithE() {
        User user = user("eworker", Set.of(Role.EMPLOYEE));

        assertThat(user.isExternal()).isTrue();
    }

    @Test
    void isExternal_shouldReturnFalseWhenZepUsernameDoesNotStartWithE() {
        User user = user("worker", Set.of(Role.EMPLOYEE));

        assertThat(user.isExternal()).isFalse();
    }

    @Test
    void constructor_shouldAllowNullZepUsernameAndEmailForSystemActor() {
        User systemActor = new User(
                UserId.generate(),
                null,
                FullName.of("MEGA", "System"),
                null,
                null,
                EmploymentPeriods.empty(),
                Set.of(Role.SYSTEM)
        );

        assertThat(systemActor.email()).isNull();
        assertThat(systemActor.zepUsername()).isNull();
        assertThat(systemActor.isSystemActor()).isTrue();
        assertThat(systemActor.isActiveIn(java.time.YearMonth.of(2026, 3))).isFalse();
    }

    @Test
    void constructor_shouldRejectNullZepUsernameAndEmailForRegularUser() {
        ThrowableAssert.ThrowingCallable throwingCallable = () -> new User(
                UserId.generate(),
                null,
                FullName.of("Regular", "User"),
                null,
                null,
                EmploymentPeriods.empty(),
                Set.of(Role.EMPLOYEE)
        );

        assertThatThrownBy(throwingCallable).isInstanceOf(NullPointerException.class);
    }

    private User user(Set<Role> roles) {
        return user("employee", roles);
    }

    private User user(String username, Set<Role> roles) {
        return new User(
                UserId.generate(),
                Email.of(username + "@example.com"),
                FullName.of("Employee", "User"),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.now().minusYears(1), null)),
                roles
        );
    }
}
