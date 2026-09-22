package com.gepardec.mega.hexagon.shared.adapter.inbound.rest;

import io.quarkiverse.httpproblem.validation.HttpValidationProblem;
import io.quarkiverse.httpproblem.validation.Violation;
import jakarta.ws.rs.core.Response;

import java.util.List;

public final class RequestValidationProblems {

    private RequestValidationProblems() {
    }

    public static HttpValidationProblem invalid(Violation.In in, String field, String message) {
        return new HttpValidationProblem(
                Response.Status.BAD_REQUEST.getStatusCode(),
                Response.Status.BAD_REQUEST.getReasonPhrase(),
                List.of(in.field(field).message(message))
        );
    }
}
