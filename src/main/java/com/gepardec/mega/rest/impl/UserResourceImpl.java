package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.interceptor.MegaRolesAllowed;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.rest.api.UserResource;
import com.gepardec.mega.rest.mapper.UserMapper;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;


@RequestScoped
@Authenticated
@MegaRolesAllowed(Role.EMPLOYEE)
public class UserResourceImpl implements UserResource {

    @Inject
    UserContext userContext;

    @Inject
    UserMapper userMapper;

    @Override
    public Response get() {
        return Response.ok(userMapper.mapToDto(userContext.getUser())).build();
    }
}
