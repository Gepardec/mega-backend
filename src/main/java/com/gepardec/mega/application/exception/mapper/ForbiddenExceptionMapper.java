package com.gepardec.mega.application.exception.mapper;

import com.gepardec.mega.application.exception.ForbiddenException;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;

import java.util.Optional;

@ApplicationScoped
@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    @Inject
    Logger logger;

    @Context
    UriInfo uriInfo;

    @Inject
    UserContext userContext;

    @Override
    public Response toResponse(ForbiddenException exception) {
        logger.warn("Forbidden access of user '{}' on resource: '{}' with message: '{}'",
                Optional.ofNullable(userContext.getUser()).map(User::getEmail).orElse("unknown"),
                uriInfo.getPath(),
                exception.getMessage());
        return Response.status(Response.Status.FORBIDDEN).build();
    }
}
