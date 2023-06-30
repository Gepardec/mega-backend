package com.gepardec.mega.application.exception.mapper;

import com.gepardec.mega.application.exception.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;

@ApplicationScoped
@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {

    @Inject
    Logger logger;

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(UnauthorizedException exception) {
        logger.warn("Unauthorized access on resource: '{}' with message: '{}'", uriInfo.getPath(), exception.getMessage());
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
