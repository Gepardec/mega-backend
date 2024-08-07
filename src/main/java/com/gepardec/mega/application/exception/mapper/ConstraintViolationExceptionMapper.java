package com.gepardec.mega.application.exception.mapper;

import com.gepardec.mega.domain.model.ValidationViolation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Inject
    Logger logger;

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        logger.info("Constraint violation(s) on resource: '{}'", uriInfo.getPath());
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(createResponseEntityForConstraintViolationException(exception))
                .build();
    }

    private List<ValidationViolation> createResponseEntityForConstraintViolationException(final ConstraintViolationException exception) {
        final Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        if (violations == null || violations.isEmpty()) {
            return List.of();
        }
        return violations.stream()
                .map(this::createValidationViolationForConstraintViolation)
                .toList();
    }

    private ValidationViolation createValidationViolationForConstraintViolation(final ConstraintViolation<?> violation) {
        return ValidationViolation.builder()
                .property(violation.getPropertyPath().toString())
                .message(violation.getMessage())
                .build();
    }
}
