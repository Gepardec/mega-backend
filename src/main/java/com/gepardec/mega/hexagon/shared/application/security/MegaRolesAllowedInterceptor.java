package com.gepardec.mega.hexagon.shared.application.security;

import com.gepardec.mega.hexagon.shared.domain.model.Role;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

@Interceptor
@MegaRolesAllowed
@Priority(Interceptor.Priority.APPLICATION)
public class MegaRolesAllowedInterceptor {

    @Inject
    AuthenticatedActorContext authenticatedActorContext;

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        MegaRolesAllowed annotation = invocationContext.getMethod().getAnnotation(MegaRolesAllowed.class);
        if (annotation == null) {
            annotation = invocationContext.getTarget().getClass().getAnnotation(MegaRolesAllowed.class);
        }

        Objects.requireNonNull(annotation,
                "Could not resolve authorization annotation. Do you use stereotype annotations, which are currently not supported?");

        if (isInRole(authenticatedActorContext.roles(), annotation.value())) {
            return invocationContext.proceed();
        }

        throw new ForbiddenException(
                "User has insufficient role " + authenticatedActorContext.roles()
        );
    }

    boolean isInRole(Set<Role> userRoles, Role[] allowedRoles) {
        if (userRoles == null || allowedRoles == null || allowedRoles.length == 0) {
            return false;
        }

        return userRoles.stream().anyMatch(role -> Arrays.asList(allowedRoles).contains(role));
    }
}
