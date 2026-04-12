package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ApiError;
import com.gepardec.mega.hexagon.worktime.adapter.inbound.rest.error.WorkTimeAuthenticatedActorResolutionException;
import com.gepardec.mega.hexagon.worktime.adapter.inbound.rest.error.WorkTimeRestAdapterException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@ApplicationScoped
@Provider
public class WorkTimeRestAdapterExceptionMapper implements ExceptionMapper<WorkTimeRestAdapterException> {

    @Override
    public Response toResponse(WorkTimeRestAdapterException exception) {
        Response.Status status = exception instanceof WorkTimeAuthenticatedActorResolutionException
                ? Response.Status.FORBIDDEN
                : Response.Status.BAD_REQUEST;

        return Response.status(status)
                .entity(new ApiError().message(exception.getMessage()))
                .build();
    }
}
