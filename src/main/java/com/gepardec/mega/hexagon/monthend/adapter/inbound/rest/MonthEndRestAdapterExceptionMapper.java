package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ApiErrorDto;
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
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiErrorDto().message(exception.getMessage()))
                .build();
    }
}
