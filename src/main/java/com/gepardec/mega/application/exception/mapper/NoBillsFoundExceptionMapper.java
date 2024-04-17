package com.gepardec.mega.application.exception.mapper;

import com.gepardec.mega.application.exception.NoBillsFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class NoBillsFoundExceptionMapper implements ExceptionMapper<NoBillsFoundException> {
    @Override
    public Response toResponse(NoBillsFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity("404 Not Found" + exception.getMessage())
                .build();
    }
}
