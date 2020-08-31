package com.gepardec.mega.application.exception.mapper;

import com.gepardec.mega.rest.model.ConstraintViolationResponse;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.ResourceBundle;

@ApplicationScoped
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Inject
    Logger logger;

    @Context
    UriInfo uriInfo;

    @Inject
    ResourceBundle resourceBundle;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        logger.info("Constraint violation(s) on resource: '{}'", uriInfo.getPath());
        return Response.status(HttpStatus.SC_BAD_REQUEST)
                .entity(ConstraintViolationResponse.invalid(resourceBundle.getString("response.exception.bad-request"),
                        exception.getConstraintViolations()))
                .build();
    }
}
