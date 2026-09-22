package com.gepardec.mega.hexagon.shared.adapter.inbound.rest;

import com.gepardec.mega.hexagon.shared.domain.error.DomainException;
import com.gepardec.mega.hexagon.shared.domain.error.ErrorCategory;
import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DomainProblemMapper extends ExceptionMapperBase<DomainException> {

    @Inject
    public DomainProblemMapper(PostProcessorsRegistry postProcessorsRegistry) {
        super(postProcessorsRegistry);
    }

    @Override
    protected HttpProblem toProblem(DomainException exception) {
        return HttpProblem.builder(HttpProblem.valueOf(toStatus(exception.category())))
                .withDetail(exception.getMessage())
                .with("code", exception.code())
                .build();
    }

    private static Response.Status toStatus(ErrorCategory category) {
        return switch (category) {
            case NOT_FOUND -> Response.Status.NOT_FOUND;
            case FORBIDDEN -> Response.Status.FORBIDDEN;
            case INVALID -> Response.Status.BAD_REQUEST;
        };
    }
}
