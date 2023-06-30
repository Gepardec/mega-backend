package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.interceptor.RolesAllowed;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.rest.api.UserResource;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;


@RequestScoped
@Authenticated
@RolesAllowed(Role.EMPLOYEE)
public class UserResourceImpl implements UserResource {

    @Inject
    UserContext userContext;

    @Override
    public Response get() {
        return Response.ok(userContext.getUser()).build();
    }
}
