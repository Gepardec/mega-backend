package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.PersonioId;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
class UserRepositoryAdapterTest {

    @Inject
    UserRepositoryAdapter userRepositoryAdapter;

    @Test
    void saveAll_andFindByZepUsername_returnsUser() {
        User user = user("jdoe", Set.of(Role.EMPLOYEE), null);

        userRepositoryAdapter.saveAll(List.of(user));

        Optional<User> found = userRepositoryAdapter.findByZepUsername(ZepUsername.of("jdoe"));
        assertThat(found).isPresent();
        assertThat(found.get().zepUsername()).isEqualTo(ZepUsername.of("jdoe"));
        assertThat(found.get().roles()).contains(Role.EMPLOYEE);
        assertThat(found.get().employmentPeriods().employmentPeriods()).hasSize(1);
    }

    @Test
    void findByZepUsername_returnsEmptyForUnknownUsername() {
        Optional<User> found = userRepositoryAdapter.findByZepUsername(ZepUsername.of("unknown"));

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_returnsAllSavedUsers() {
        User user1 = user("user1", Set.of(Role.EMPLOYEE), null);
        User user2 = user("user2", Set.of(Role.EMPLOYEE, Role.OFFICE_MANAGEMENT), PersonioId.of(77));

        userRepositoryAdapter.saveAll(List.of(user1, user2));

        List<User> all = userRepositoryAdapter.findAll();
        assertThat(all).hasSize(2);
        assertThat(all.stream().map(u -> u.zepUsername().value()))
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void saveAll_updatesExistingUser() {
        User initial = user("jdoe", Set.of(Role.EMPLOYEE), null);
        userRepositoryAdapter.saveAll(List.of(initial));

        User updated = new User(
                initial.id(),
                initial.email(),
                initial.name(),
                initial.zepUsername(),
                PersonioId.of(42),
                initial.employmentPeriods(),
                Set.of(Role.EMPLOYEE, Role.OFFICE_MANAGEMENT)
        );
        userRepositoryAdapter.saveAll(List.of(updated));

        Optional<User> found = userRepositoryAdapter.findByZepUsername(ZepUsername.of("jdoe"));
        assertThat(found).isPresent();
        assertThat(found.get().personioId()).isEqualTo(PersonioId.of(42));
        assertThat(found.get().roles()).containsExactlyInAnyOrder(Role.EMPLOYEE, Role.OFFICE_MANAGEMENT);
    }

    private User user(String username, Set<Role> roles, PersonioId personioId) {
        return new User(
                UserId.generate(),
                Email.of(username + "@example.com"),
                FullName.of("John", "Doe"),
                ZepUsername.of(username),
                personioId,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2024, 1, 1), null)),
                roles
        );
    }
}
