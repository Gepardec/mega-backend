package com.gepardec.mega.hexagon.shared.application.security;

import com.gepardec.mega.hexagon.shared.domain.model.AuthenticatedActor;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import org.eclipse.microprofile.jwt.ClaimValue;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticatedActorContextTest {

    private static final String EMAIL = "employee@gepardec.com";

    @Mock
    private ClaimValue<String> emailClaim;

    @Mock
    private UserRepository userRepository;

    private AuthenticatedActorContext authenticatedActorContext;

    @BeforeEach
    void setUp() {
        authenticatedActorContext = new AuthenticatedActorContext(emailClaim, userRepository);
    }

    @Test
    void authenticatedActor_shouldResolveCurrentActor() {
        UserId actorId = UserId.of(Instancio.create(UUID.class));
        when(emailClaim.getValue()).thenReturn(EMAIL);
        when(userRepository.findByEmail(Email.of(EMAIL))).thenReturn(Optional.of(user(actorId, EMAIL)));

        AuthenticatedActor authenticatedActor = authenticatedActorContext.authenticatedActor();

        assertThat(authenticatedActor.userId()).isEqualTo(actorId);
        assertThat(authenticatedActor.email()).isEqualTo(Email.of(EMAIL));
        assertThat(authenticatedActor.roles()).containsExactly(Role.EMPLOYEE);
    }

    @Test
    void authenticatedActor_shouldResolveOnlyOncePerRequest() {
        UserId actorId = UserId.of(Instancio.create(UUID.class));
        when(emailClaim.getValue()).thenReturn(EMAIL);
        when(userRepository.findByEmail(Email.of(EMAIL))).thenReturn(Optional.of(user(actorId, EMAIL)));

        assertThat(authenticatedActorContext.userId()).isEqualTo(actorId);
        assertThat(authenticatedActorContext.roles()).containsExactly(Role.EMPLOYEE);

        verify(userRepository, times(1)).findByEmail(Email.of(EMAIL));
    }

    @Test
    void authenticatedActor_shouldRejectMissingEmail() {
        when(emailClaim.getValue()).thenReturn(" ");

        assertThatThrownBy(() -> authenticatedActorContext.authenticatedActor())
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("email is not available");
    }

    @Test
    void authenticatedActor_shouldRejectUnknownEmail() {
        when(emailClaim.getValue()).thenReturn(EMAIL);
        when(userRepository.findByEmail(Email.of(EMAIL))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticatedActorContext.authenticatedActor())
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("actor not found");
    }

    private User user(UserId actorId, String email) {
        return new User(
                actorId,
                Email.of(email),
                FullName.of("Ada", "Lovelace"),
                ZepUsername.of("ada"),
                null,
                EmploymentPeriods.empty(),
                Set.of(Role.EMPLOYEE)
        );
    }
}
