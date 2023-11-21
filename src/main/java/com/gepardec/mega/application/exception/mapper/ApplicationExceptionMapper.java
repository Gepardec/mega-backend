package com.gepardec.mega.application.exception.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;

@Provider
public class ApplicationExceptionMapper implements jakarta.ws.rs.ext.ExceptionMapper<Exception> {

    @Inject
    Logger log;

    @Override
    public Response toResponse(Exception exception) {
        log.error("An unhandled exception occurred", exception);
        return Response.serverError().build();
    }
}
