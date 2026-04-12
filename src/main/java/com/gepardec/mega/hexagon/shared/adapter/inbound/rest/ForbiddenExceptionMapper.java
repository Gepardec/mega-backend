package com.gepardec.mega.hexagon.shared.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ApiError;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.ForbiddenException;
import com.gepardec.mega.hexagon.shared.domain.model.AuthenticatedActor;
import com.gepardec.mega.hexagon.user.domain.model.Email;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Optional;

@ApplicationScoped
@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    @Context
    UriInfo uriInfo;

    @Inject
    AuthenticatedActorContext authenticatedActorContext;

    @Override
    public Response toResponse(ForbiddenException exception) {
        Email userEmail = Optional.ofNullable(authenticatedActorContext.authenticatedActor())
                .map(AuthenticatedActor::email)
                .orElse(Email.of("unknown"));
        Log.warnf("Forbidden access of user '%s' on resource: '%s' with message: '%s'",
                userEmail,
                uriInfo.getPath(),
                exception.getMessage()
        );
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new ApiError().message(exception.getMessage()))
                .build();
    }
}
