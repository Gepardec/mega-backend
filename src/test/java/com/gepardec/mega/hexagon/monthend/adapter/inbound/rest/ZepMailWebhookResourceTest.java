package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateClarificationFromZepMailUseCase;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@QuarkusTest
@TestSecurity(user = "pubsub")
class ZepMailWebhookResourceTest {

    @InjectMock
    CreateClarificationFromZepMailUseCase createClarificationFromZepMailUseCase;

    @Test
    void gmailMessageReceivedWebhook_shouldReturnOk_whenUseCaseSucceeds() {
        given()
                .body("{}")
                .post("/pubsub/message-received")
                .then()
                .statusCode(200);

        verify(createClarificationFromZepMailUseCase).create();
    }

    @Test
    void gmailMessageReceivedWebhook_shouldReturnProblemWithoutInternals_whenUseCaseFails() {
        doThrow(new IllegalStateException("secret internal failure"))
                .when(createClarificationFromZepMailUseCase)
                .create();

        given()
                .body("{}")
                .post("/pubsub/message-received")
                .then()
                .statusCode(500)
                .contentType("application/problem+json")
                .body("status", is(500))
                .body("code", nullValue())
                .body("detail", nullValue())
                .body(not(containsString("secret internal failure")));
    }
}
