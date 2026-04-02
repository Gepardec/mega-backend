package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.port.outbound.AuthenticatedActorEmailPort;
import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.RegularWorkingTimes;
import com.gepardec.mega.hexagon.user.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepProfile;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CurrentMonthEndActorResolverTest {

    private AuthenticatedActorEmailPort authenticatedActorEmailPort;
    private UserRepository userRepository;
    private CurrentMonthEndActorResolver resolver;

    @BeforeEach
    void setUp() {
        authenticatedActorEmailPort = mock(AuthenticatedActorEmailPort.class);
        userRepository = mock(UserRepository.class);
        resolver = new CurrentMonthEndActorResolver(authenticatedActorEmailPort, userRepository);
    }

    @Test
    void resolveCurrentActorId_shouldReturnUserId_whenEmailMatchesHexagonUser() {
        User user = user("employee");
        when(authenticatedActorEmailPort.currentEmail()).thenReturn(user.getEmail().value());
        when(userRepository.findByEmail(Email.of(user.getEmail().value()))).thenReturn(Optional.of(user));

        UserId result = resolver.resolveCurrentActorId();

        assertThat(result).isEqualTo(user.getId());
        verify(userRepository).findByEmail(Email.of(user.getEmail().value()));
    }

    @Test
    void resolveCurrentActorId_shouldThrow_whenNoHexagonUserMatchesAuthenticatedEmail() {
        when(authenticatedActorEmailPort.currentEmail()).thenReturn("missing@example.com");
        when(userRepository.findByEmail(Email.of("missing@example.com"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolveCurrentActorId())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing@example.com");
    }

    private User user(String username) {
        return User.create(
                UserId.of(Instancio.create(UUID.class)),
                new ZepProfile(
                        username,
                        username + "@example.com",
                        "Test",
                        "User",
                        null,
                        null,
                        null,
                        null,
                        null,
                        new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2020, 1, 1), null)),
                        RegularWorkingTimes.empty()
                ),
                Set.of(Role.EMPLOYEE)
        );
    }
}
