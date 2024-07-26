package com.gepardec.mega.rest.probes;

import com.gepardec.mega.zep.rest.client.ZepLivenessRestClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ReadynessProbeTest {

    @InjectMock
    @RestClient
    ZepLivenessRestClient zepLivenessRestClient;

    @BeforeEach
    void setUp() {
        Mockito.when(zepLivenessRestClient.liveness())
                .thenReturn(Response.ok().build());
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
    void health_whenCalled_thenReturnsHttpStatusOK() {
        given().when()
                .get("/health")
                .then().assertThat().statusCode(HttpStatus.SC_OK);
    }
}
