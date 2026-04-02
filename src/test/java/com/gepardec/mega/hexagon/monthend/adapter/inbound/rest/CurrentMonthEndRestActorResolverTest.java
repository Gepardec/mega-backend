package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.error.MonthEndAuthenticatedActorResolutionException;
import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.UserStatus;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentMonthEndRestActorResolverTest {

    private static final String EMAIL = "employee@gepardec.com";

    @Mock
    private ClaimValue<String> emailClaim;

    @Mock
    private UserRepository userRepository;

    private CurrentMonthEndRestActorResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CurrentMonthEndRestActorResolver(emailClaim, userRepository);
    }

    @Test
    void resolveCurrentActorId_shouldReturnResolvedUserId() {
        UserId actorId = UserId.of(Instancio.create(UUID.class));
        when(emailClaim.getValue()).thenReturn(EMAIL);
        when(userRepository.findByEmail(Email.of(EMAIL))).thenReturn(Optional.of(user(actorId, EMAIL)));

        UserId resolvedActorId = resolver.resolveCurrentActorId();

        assertThat(resolvedActorId).isEqualTo(actorId);
    }

    @Test
    void resolveCurrentActorId_shouldRejectMissingEmail() {
        when(emailClaim.getValue()).thenReturn("  ");

        assertThatThrownBy(() -> resolver.resolveCurrentActorId())
                .isInstanceOf(MonthEndAuthenticatedActorResolutionException.class)
                .hasMessageContaining("email is not available");
    }

    @Test
    void resolveCurrentActorId_shouldRejectUnknownEmail() {
        when(emailClaim.getValue()).thenReturn(EMAIL);
        when(userRepository.findByEmail(Email.of(EMAIL))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolveCurrentActorId())
                .isInstanceOf(MonthEndAuthenticatedActorResolutionException.class)
                .hasMessageContaining("actor not found");
    }

    private User user(UserId actorId, String email) {
        return User.reconstitute(
                actorId,
                Email.of(email),
                FullName.of("Ada", "Lovelace"),
                UserStatus.ACTIVE,
                Set.of(Role.EMPLOYEE),
                null,
                null
        );
    }
}
