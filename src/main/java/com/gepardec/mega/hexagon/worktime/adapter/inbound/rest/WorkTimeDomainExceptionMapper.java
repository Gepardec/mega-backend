package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ApiErrorDto;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeException;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeUserNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@ApplicationScoped
@Provider
public class WorkTimeDomainExceptionMapper implements ExceptionMapper<WorkTimeException> {

    @Override
    public Response toResponse(WorkTimeException exception) {
        Response.Status status = exception instanceof WorkTimeUserNotFoundException
                ? Response.Status.NOT_FOUND
                : Response.Status.BAD_REQUEST;

        return Response.status(status)
                .entity(new ApiErrorDto().message(exception.getMessage()))
                .build();
    }
}
