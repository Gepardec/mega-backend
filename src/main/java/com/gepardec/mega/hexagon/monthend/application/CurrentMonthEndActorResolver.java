package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.port.outbound.AuthenticatedActorEmailPort;
import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CurrentMonthEndActorResolver {

    private final AuthenticatedActorEmailPort authenticatedActorEmailPort;
    private final UserRepository userRepository;

    @Inject
    public CurrentMonthEndActorResolver(
            AuthenticatedActorEmailPort authenticatedActorEmailPort,
            UserRepository userRepository
    ) {
        this.authenticatedActorEmailPort = authenticatedActorEmailPort;
        this.userRepository = userRepository;
    }

    public UserId resolveCurrentActorId() {
        String email = authenticatedActorEmailPort.currentEmail();
        return userRepository.findByEmail(Email.of(email))
                .map(User::getId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "authenticated month-end actor not found for email: " + email
                ));
    }
}
