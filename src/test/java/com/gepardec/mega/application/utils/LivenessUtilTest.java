package com.gepardec.mega.application.utils;

import com.gepardec.mega.application.health.LivenessUtil;
import com.gepardec.mega.personio.PersonioHealthClient;
import com.gepardec.mega.zep.rest.client.ZepHealthRestClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@QuarkusTest
public class LivenessUtilTest {
    @Inject
    LivenessUtil livenessUtil;

    @InjectMock
    @RestClient
    PersonioHealthClient personioHealthClient;

    @InjectMock
    @RestClient
    ZepHealthRestClient zepHealthRestClient;


    @Test
    void getResponseForPersonio_whenPersonioIsUp_returnHealthCheckResponseWithStatusUp() {
        when(personioHealthClient.health()).thenReturn(getOkResponse());
        HealthCheckResponse actual = livenessUtil.getResponseForPersonio();

       assertThat(actual.getName()).isEqualTo("Personio Liveness");
       assertThat(actual.getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
    }

    @Test
    void getResponseForPersonio_whenPersonioIsDown_returnHealthCheckResponseWithStatusDown() {
        when(personioHealthClient.health()).thenReturn(getErrorResponse());
        HealthCheckResponse actual = livenessUtil.getResponseForPersonio();

        assertThat(actual.getName()).isEqualTo("Personio Liveness");
        assertThat(actual.getStatus()).isEqualTo(HealthCheckResponse.Status.DOWN);
    }

    @Test
    void getResponseForZep_whenZepIsUp_returnHealthCheckResponseWithStatusUp() {
        when(zepHealthRestClient.health()).thenReturn(getOkResponse());
        HealthCheckResponse actual = livenessUtil.getResponseForZep();

        assertThat(actual.getName()).isEqualTo("Zep Liveness");
        assertThat(actual.getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
    }

    @Test
    void getResponseForZep_whenZepIsDown_returnHealthCheckResponseWithStatusDown() {
        when(zepHealthRestClient.health()).thenReturn(getErrorResponse());
        HealthCheckResponse actual = livenessUtil.getResponseForZep();

        assertThat(actual.getName()).isEqualTo("Zep Liveness");
        assertThat(actual.getStatus()).isEqualTo(HealthCheckResponse.Status.DOWN);
    }

    private Response getOkResponse() {
        return Response.ok().build();
    }

    private Response getErrorResponse() {
        return Response.serverError().build();
    }
}
