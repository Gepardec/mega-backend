package com.gepardec.mega.application.producer;

import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.service.api.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.ClaimValue;
import org.eclipse.microprofile.jwt.Claims;

@ApplicationScoped
public class UserContextProducer {

    @Inject
    @Claim(standard = Claims.email)
    ClaimValue<String> email;

    @Inject
    UserService userService;

    @Produces
    @RequestScoped
    UserContext createUserContext() {
        final User user = userService.findUserForEmail(email.getValue());
        return UserContext.builder()
                .user(user)
                .build();
    }
}
