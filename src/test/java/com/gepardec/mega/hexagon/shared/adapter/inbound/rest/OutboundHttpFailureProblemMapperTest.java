package com.gepardec.mega.hexagon.shared.adapter.inbound.rest;

import io.quarkiverse.httpproblem.HttpProblem;
import io.quarkiverse.httpproblem.postprocessing.PostProcessorsRegistry;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OutboundHttpFailureProblemMapperTest {

    @Mock
    private PostProcessorsRegistry postProcessorsRegistry;

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 404, 429, 500, 503})
    void toProblem_shouldAnswerBadGatewayWithoutDetailOrCode_whateverTheDownstreamStatus(int downstreamStatus) {
        // Given
        OutboundHttpFailureProblemMapper mapper = new OutboundHttpFailureProblemMapper(postProcessorsRegistry);
        ClientWebApplicationException exception = new ClientWebApplicationException(
                "Received: 'status code " + downstreamStatus + "' when invoking: Rest Client method: 'ZepClient#get'",
                downstreamStatus
        );

        // When
        HttpProblem problem = mapper.toProblem(exception);

        // Then
        assertThat(problem.getStatusCode()).isEqualTo(502);
        assertThat(problem.getTitle()).isEqualTo("Bad Gateway");
        assertThat(problem.getDetail()).isNull();
        assertThat(problem.getParameters()).doesNotContainKey("code");
        assertThat(problem.getHeaders()).isEmpty();
    }
}
