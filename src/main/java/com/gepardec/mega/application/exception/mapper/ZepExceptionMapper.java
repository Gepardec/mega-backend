package com.gepardec.mega.application.exception.mapper;

import com.gepardec.mega.zep.ZepServiceException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;

@Provider
public class ZepExceptionMapper implements ExceptionMapper<ZepServiceException> {

    @Inject
    Logger log;

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ZepServiceException exception) {
        log.error("ZEP Service error on resource: '{}' with message: '{}'", uriInfo.getPath(), exception.getMessage());
        return Response.serverError().build();
    }
}
