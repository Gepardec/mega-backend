package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ApiErrorDto;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndActorNotAuthorizedException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndClarificationNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndTaskNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@ApplicationScoped
@Provider
public class MonthEndDomainExceptionMapper implements ExceptionMapper<MonthEndException> {

    @Override
    public Response toResponse(MonthEndException exception) {
        Response.Status status;
        if (exception instanceof MonthEndTaskNotFoundException
                || exception instanceof MonthEndClarificationNotFoundException) {
            status = Response.Status.NOT_FOUND;
        } else if (exception instanceof MonthEndActorNotAuthorizedException) {
            status = Response.Status.FORBIDDEN;
        } else {
            status = Response.Status.BAD_REQUEST;
        }

        return Response.status(status)
                .entity(new ApiErrorDto().message(exception.getMessage()))
                .build();
    }
}
