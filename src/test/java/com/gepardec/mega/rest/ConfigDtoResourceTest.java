package com.gepardec.mega.rest;

import com.gepardec.mega.application.configuration.ApplicationConfig;
import com.gepardec.mega.application.configuration.OAuthConfig;
import com.gepardec.mega.application.configuration.ZepConfig;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestSecurity(user = "test")
@OidcSecurity(claims = {
        @Claim(key = "email", value = "test@gepardec.com")
})
class ConfigDtoResourceTest {

    @Inject
    OAuthConfig oAuthConfig;

    @Inject
    ApplicationConfig applicationConfig;

    @Inject
    ZepConfig zepConfig;

    @Test
    void get_whenPOST_thenReturnsHttpStatusMETHOD_NOT_ALLOWED() {
        given().contentType(ContentType.TEXT)
                .post("/config")
                .then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void get_whenGET_thenReturnsConfig() {
        given().contentType(ContentType.TEXT)
                .get("/config")
                .then().statusCode(HttpStatus.SC_OK)
                .body("zepOrigin", equalTo(zepConfig.getUrlForFrontend()))
                .body("clientId", equalTo(oAuthConfig.getClientId()))
                .body("issuer", equalTo(oAuthConfig.getIssuer()))
                .body("scope", equalTo(oAuthConfig.getScope()))
                .body("version", equalTo(applicationConfig.getVersion()));
    }
}
