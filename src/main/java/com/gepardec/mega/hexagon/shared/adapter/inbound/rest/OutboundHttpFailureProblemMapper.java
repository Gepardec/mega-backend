package com.gepardec.mega.hexagon.shared.adapter.inbound.rest;

import io.quarkiverse.httpproblem.ExceptionMapperBase;
import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

/**
 * Answers a failed call to an external system (an outbound REST client that received an HTTP error
 * status) with {@code 502}. Without it, the extension's {@code WebApplicationExceptionMapper} would
 * pass the external system's status, headers and the client's message on to our caller.
 */
@Provider
public class OutboundHttpFailureProblemMapper extends ExceptionMapperBase<ClientWebApplicationException> {

    @Inject
    public OutboundHttpFailureProblemMapper(PostProcessorsRegistry postProcessorsRegistry) {
        super(postProcessorsRegistry);
    }

    @Override
    protected HttpProblem toProblem(ClientWebApplicationException exception) {
        return HttpProblem.valueOf(Response.Status.BAD_GATEWAY);
    }
}
