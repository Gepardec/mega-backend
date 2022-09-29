package com.gepardec.mega.application.producer;

import com.gepardec.mega.application.exception.UnauthorizedException;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.service.api.UserService;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.ClaimValue;
import org.eclipse.microprofile.jwt.Claims;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

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
