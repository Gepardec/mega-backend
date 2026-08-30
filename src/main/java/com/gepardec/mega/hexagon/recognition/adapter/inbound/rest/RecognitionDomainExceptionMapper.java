package com.gepardec.mega.hexagon.recognition.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ApiErrorDto;
import com.gepardec.mega.hexagon.recognition.domain.error.RecognitionException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@ApplicationScoped
@Provider
public class RecognitionDomainExceptionMapper implements ExceptionMapper<RecognitionException> {

    @Override
    public Response toResponse(RecognitionException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiErrorDto().message(exception.getMessage()))
                .build();
    }
}
