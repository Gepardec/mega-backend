package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ApiError;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.error.MonthEndAuthenticatedActorResolutionException;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.error.MonthEndRestAdapterException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@ApplicationScoped
@Provider
public class MonthEndRestAdapterExceptionMapper implements ExceptionMapper<MonthEndRestAdapterException> {

    @Override
    public Response toResponse(MonthEndRestAdapterException exception) {
        Response.Status status = exception instanceof MonthEndAuthenticatedActorResolutionException
                ? Response.Status.FORBIDDEN
                : Response.Status.BAD_REQUEST;

        return Response.status(status)
                .entity(new ApiError().message(exception.getMessage()))
                .build();
    }
}
