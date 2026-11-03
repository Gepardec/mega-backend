package com.gepardec.mega.hexagon.shared.adapter.inbound.rest;

import com.gepardec.mega.hexagon.shared.domain.error.DomainException;
import com.gepardec.mega.hexagon.shared.domain.error.ErrorCategory;
import com.gepardec.mega.hexagon.shared.domain.error.ErrorCode;
import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DomainProblemMapper extends ExceptionMapperBase<DomainException> {

    private static final int UNPROCESSABLE_CONTENT = 422;

    @Inject
    public DomainProblemMapper(PostProcessorsRegistry postProcessorsRegistry) {
        super(postProcessorsRegistry);
    }

    /**
     * A problem with the status, title and {@code code} for the given error code. The only place where an
     * error category becomes an HTTP status; adapters that build problems themselves start from here.
     */
    public static HttpProblem.Builder problemOf(ErrorCode errorCode) {
        return statusOf(errorCode.category()).with("code", errorCode.code());
    }

    @Override
    protected HttpProblem toProblem(DomainException exception) {
        return problemOf(exception.errorCode())
                .withDetail(exception.getMessage())
                .build();
    }

    private static HttpProblem.Builder statusOf(ErrorCategory category) {
        return switch (category) {
            case NOT_FOUND -> HttpProblem.builder(HttpProblem.valueOf(Response.Status.NOT_FOUND));
            case FORBIDDEN -> HttpProblem.builder(HttpProblem.valueOf(Response.Status.FORBIDDEN));
            case CONFLICT -> HttpProblem.builder(HttpProblem.valueOf(Response.Status.CONFLICT));
            // Jakarta REST has no constant for 422.
            case UNPROCESSABLE -> HttpProblem.builder()
                    .withStatus(UNPROCESSABLE_CONTENT)
                    .withTitle("Unprocessable Content");
            case INVALID -> HttpProblem.builder(HttpProblem.valueOf(Response.Status.BAD_REQUEST));
        };
    }
}
