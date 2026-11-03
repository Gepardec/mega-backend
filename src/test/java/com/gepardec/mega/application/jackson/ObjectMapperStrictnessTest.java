package com.gepardec.mega.application.jackson;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the split between strict server-side and lenient client-side deserialization. The fake upstream is a
 * test-only resource of this application; its response carries a field the client DTO doesn't declare.
 */
@QuarkusTest
class ObjectMapperStrictnessTest {

    @TestHTTPResource("/")
    URI baseUri;

    @Test
    void restClient_shouldIgnoreUnknownResponseProperties() {
        FakeUpstreamClient client = QuarkusRestClientBuilder.newBuilder()
                .baseUri(baseUri)
                .build(FakeUpstreamClient.class);

        UpstreamDto response = client.get();

        assertThat(response.name).isEqualTo("known");
    }

    @Test
    void server_shouldStillRejectUnknownRequestProperties() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "known", "addedLater", "unexpected"))
                .post("/test-upstream")
                .then()
                .statusCode(400);
    }

    @Path("/test-upstream")
    public interface FakeUpstreamClient {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        UpstreamDto get();
    }

    @Path("/test-upstream")
    public static class FakeUpstreamResource {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Map<String, String> get() {
            return Map.of("name", "known", "addedLater", "unexpected");
        }

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        public void post(UpstreamDto body) {
            // only the deserialization of the body matters
        }
    }

    public static class UpstreamDto {
        public String name;
    }
}
