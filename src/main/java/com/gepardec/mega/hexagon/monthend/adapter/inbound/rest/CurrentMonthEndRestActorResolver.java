package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.error.MonthEndAuthenticatedActorResolutionException;
import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.ClaimValue;
import org.eclipse.microprofile.jwt.Claims;

@ApplicationScoped
public class CurrentMonthEndRestActorResolver {

    private final ClaimValue<String> email;
    private final UserRepository userRepository;

    @Inject
    public CurrentMonthEndRestActorResolver(
            @Claim(standard = Claims.email) ClaimValue<String> email,
            UserRepository userRepository
    ) {
        this.email = email;
        this.userRepository = userRepository;
    }

    public UserId resolveCurrentActorId() {
        String value = email.getValue();
        if (value == null || value.isBlank()) {
            throw new MonthEndAuthenticatedActorResolutionException("authenticated actor email is not available");
        }

        return userRepository.findByEmail(Email.of(value))
                .map(User::getId)
                .orElseThrow(() -> new MonthEndAuthenticatedActorResolutionException(
                        "authenticated month-end actor not found for email: " + value
                ));
    }
}
