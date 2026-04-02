package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.port.outbound.AuthenticatedActorEmailPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.ClaimValue;
import org.eclipse.microprofile.jwt.Claims;

@ApplicationScoped
public class AuthenticatedActorEmailAdapter implements AuthenticatedActorEmailPort {

    @Inject
    @Claim(standard = Claims.email)
    ClaimValue<String> email;

    @Override
    public String currentEmail() {
        String value = email.getValue();
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("authenticated actor email is not available");
        }
        return value;
    }
}
