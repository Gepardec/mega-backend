package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.UserStatus;
import com.gepardec.mega.hexagon.user.domain.model.ZepProfile;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.RegularWorkingTimes;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
class UserRepositoryAdapterTest {

    @Inject
    UserRepositoryAdapter userRepositoryAdapter;

    private ZepProfile profile(String username) {
        return new ZepProfile(username, username + "@example.com", "John", "Doe",
                null, null, null, null, null, EmploymentPeriods.empty(), RegularWorkingTimes.empty());
    }

    @Test
    void saveAll_andFindByZepUsername_returnsUser() {
        User user = User.create(UserId.generate(), profile("jdoe"), Set.of(Role.EMPLOYEE));

        userRepositoryAdapter.saveAll(List.of(user));

        Optional<User> found = userRepositoryAdapter.findByZepUsername("jdoe");
        assertThat(found).isPresent();
        assertThat(found.get().zepProfile().username()).isEqualTo("jdoe");
        assertThat(found.get().status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(found.get().roles()).contains(Role.EMPLOYEE);
    }

    @Test
    void findByZepUsername_returnsEmptyForUnknownUsername() {
        Optional<User> found = userRepositoryAdapter.findByZepUsername("unknown");

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_returnsAllSavedUsers() {
        User user1 = User.create(UserId.generate(), profile("user1"), Set.of(Role.EMPLOYEE));
        User user2 = User.create(UserId.generate(), profile("user2"), Set.of(Role.EMPLOYEE, Role.OFFICE_MANAGEMENT));

        userRepositoryAdapter.saveAll(List.of(user1, user2));

        List<User> all = userRepositoryAdapter.findAll();
        assertThat(all).hasSize(2);
        assertThat(all.stream().map(u -> u.zepProfile().username()))
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void saveAll_updatesExistingUser() {
        User user = User.create(UserId.generate(), profile("jdoe"), Set.of(Role.EMPLOYEE));
        userRepositoryAdapter.saveAll(List.of(user));

        user.deactivate();
        userRepositoryAdapter.saveAll(List.of(user));

        Optional<User> found = userRepositoryAdapter.findByZepUsername("jdoe");
        assertThat(found).isPresent();
        assertThat(found.get().status()).isEqualTo(UserStatus.INACTIVE);
    }
}
