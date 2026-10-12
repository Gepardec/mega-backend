package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ApiErrorDto;
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
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiErrorDto().message(exception.getMessage()))
                .build();
    }
}
