package com.gepardec.mega.application.utils;

import com.gepardec.mega.application.health.HealthCheckUtil;
import com.gepardec.mega.personio.PersonioLivenessClient;
import com.gepardec.mega.zep.rest.client.ZepLivenessRestClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@QuarkusTest
public class HealthCheckUtilTest {

    @InjectMock
    @RestClient
    PersonioLivenessClient personioLivenessClient;

    @InjectMock
    @RestClient
    ZepLivenessRestClient zepLivenessRestClient;

    @Test
    void checkApi_whenPersonioIsUp_returnHealthCheckResponseWithStatusUp() {
        when(personioLivenessClient.liveness()).thenReturn(getOkResponse());
        HealthCheckResponse actual = HealthCheckUtil.checkApi("Personio", personioLivenessClient);

        assertThat(actual.getName()).isEqualTo("Personio Liveness");
        assertThat(actual.getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
    }

    @Test
    void checkApi_whenPersonioIsDown_returnHealthCheckResponseWithStatusDown() {
        when(personioLivenessClient.liveness()).thenReturn(getErrorResponse());
        HealthCheckResponse actual = HealthCheckUtil.checkApi("Personio", personioLivenessClient);

        assertThat(actual.getName()).isEqualTo("Personio Liveness");
        assertThat(actual.getStatus()).isEqualTo(HealthCheckResponse.Status.DOWN);
    }

    @Test
    void getResponseForZep_whenZepIsUp_returnHealthCheckResponseWithStatusUp() {
        when(zepLivenessRestClient.liveness()).thenReturn(getOkResponse());
        HealthCheckResponse actual = HealthCheckUtil.checkApi("Zep", zepLivenessRestClient);

        assertThat(actual.getName()).isEqualTo("Zep Liveness");
        assertThat(actual.getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
    }

    @Test
    void getResponseForZep_whenZepIsDown_returnHealthCheckResponseWithStatusDown() {
        when(zepLivenessRestClient.liveness()).thenReturn(getErrorResponse());
        HealthCheckResponse actual = HealthCheckUtil.checkApi("Zep", zepLivenessRestClient);

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
