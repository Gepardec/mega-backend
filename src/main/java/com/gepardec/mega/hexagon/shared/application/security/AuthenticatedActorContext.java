package com.gepardec.mega.hexagon.shared.application.security;

import com.gepardec.mega.hexagon.shared.domain.model.AuthenticatedActor;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.ClaimValue;
import org.eclipse.microprofile.jwt.Claims;

import java.util.Set;

@RequestScoped
public class AuthenticatedActorContext {

    private final ClaimValue<String> emailClaim;
    private final UserRepository userRepository;
    private AuthenticatedActor authenticatedActor;

    @Inject
    public AuthenticatedActorContext(
            @Claim(standard = Claims.email) ClaimValue<String> emailClaim,
            UserRepository userRepository
    ) {
        this.emailClaim = emailClaim;
        this.userRepository = userRepository;
    }

    public AuthenticatedActor authenticatedActor() {
        if (authenticatedActor == null) {
            authenticatedActor = resolveAuthenticatedActor();
        }
        return authenticatedActor;
    }

    public UserId userId() {
        return authenticatedActor().userId();
    }

    public Set<Role> roles() {
        return authenticatedActor().roles();
    }

    public boolean hasRole(Role role) {
        return role != null && roles().contains(role);
    }

    private AuthenticatedActor resolveAuthenticatedActor() {
        String value = emailClaim.getValue();
        if (value == null || value.isBlank()) {
            throw new ForbiddenException("authenticated actor email is not available");
        }

        User user = userRepository.findByEmail(Email.of(value))
                .orElseThrow(() -> new ForbiddenException(
                        "authenticated actor not found for email: " + value
                ));

        return new AuthenticatedActor(user.id(), user.email(), user.roles());
    }
}
