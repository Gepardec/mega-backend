package com.gepardec.mega.hexagon.project.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.adapter.outbound.UserRepositoryAdapter;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.User;
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
class UserIdentityLookupAdapterTest {

    @Inject
    UserIdentityLookupAdapter userIdentityLookupAdapter;

    @Inject
    UserRepositoryAdapter userRepositoryAdapter;

    @Test
    void findUserIdByZepUsername_shouldReturnResolvedId() {
        User user = user("jdoe");
        userRepositoryAdapter.saveAll(List.of(user));

        Optional<UserId> resolved = userIdentityLookupAdapter.findUserIdByZepUsername(ZepUsername.of("jdoe"));

        assertThat(resolved).contains(user.id());
    }

    @Test
    void findUserIdByZepUsername_shouldReturnEmptyForUnknownUsername() {
        Optional<UserId> resolved = userIdentityLookupAdapter.findUserIdByZepUsername(ZepUsername.of("unknown"));

        assertThat(resolved).isEmpty();
    }

    private User user(String username) {
        return new User(
                UserId.generate(),
                Email.of(username + "@example.com"),
                FullName.of("John", "Doe"),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2024, 1, 1), null)),
                Set.of(Role.EMPLOYEE)
        );
    }
}
