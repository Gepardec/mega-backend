package com.gepardec.mega.rest.probes;

import com.gepardec.mega.personio.PersonioLivenessClient;
import com.gepardec.mega.zep.rest.client.ZepLivenessRestClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

@QuarkusTest
class HealthCheckTest {

    @InjectMock
    @RestClient
    ZepLivenessRestClient zepLivenessRestClient;

    @InjectMock
    @RestClient
    PersonioLivenessClient personioLivenessClient;

    @BeforeEach
    void setUp() {
        when(zepLivenessRestClient.liveness()).thenReturn(Response.ok().build());
        when(personioLivenessClient.liveness()).thenReturn(Response.ok().build());
    }

    @Test
    void ready_whenCalled_thenReturnsHttpStatusOK() {
        given().when()
                .get("/health/ready")
                .then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void live_whenCalled_thenReturnsHttpStatusOK() {
        given().when()
                .get("/health/live")
                .then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void well_whenCalled_thenReturnsHttpStatusOK() {
        given().when()
                .get("/health/well")
                .then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void health_whenCalled_thenReturnsHttpStatusOK() {
        given().when()
                .get("/health")
                .then().assertThat().statusCode(HttpStatus.SC_OK);
    }
}
