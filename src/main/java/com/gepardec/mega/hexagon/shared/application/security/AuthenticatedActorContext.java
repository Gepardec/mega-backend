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
    private User user;

    @Inject
    public AuthenticatedActorContext(
            @Claim(standard = Claims.email) ClaimValue<String> emailClaim,
            UserRepository userRepository
    ) {
        this.emailClaim = emailClaim;
        this.userRepository = userRepository;
    }

    public AuthenticatedActor authenticatedActor() {
        User currentUser = user();
        return new AuthenticatedActor(currentUser.id(), currentUser.email(), currentUser.roles());
    }

    public UserId userId() {
        return authenticatedActor().userId();
    }

    public User user() {
        if (user == null) {
            user = resolveUser();
        }
        return user;
    }

    public Set<Role> roles() {
        return authenticatedActor().roles();
    }

    public boolean hasRole(Role role) {
        return role != null && roles().contains(role);
    }

    private User resolveUser() {
        String value = emailClaim.getValue();
        if (value == null || value.isBlank()) {
            throw new ForbiddenException("authenticated actor email is not available");
        }

        return userRepository.findByEmail(Email.of(value))
                .orElseThrow(() -> new ForbiddenException(
                        "authenticated actor not found for email: " + value
                ));
    }
}
