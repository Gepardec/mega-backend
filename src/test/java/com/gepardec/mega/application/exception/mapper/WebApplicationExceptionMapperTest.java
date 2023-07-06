package com.gepardec.mega.application.exception.mapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebApplicationExceptionMapperTest {

    private WebApplicationExceptionMapper mapper;

    @BeforeEach
    void beforeEach() {
        mapper = new WebApplicationExceptionMapper();
    }

    @Test
    void toResponse_whenCalled_thenReturnsWebApplicationExceptionContainedResponse() {
        final Response expected = Response.status(Response.Status.CONFLICT).build();
        final Response actual = mapper.toResponse(new WebApplicationException(expected));

        assertThat(actual).isEqualTo(expected);
    }
}
