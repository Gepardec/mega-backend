package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WarningRuleSettingsPort;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSetting;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
class WorkTimeWarningRuleResourceTest {

    @InjectMock
    AuthenticatedActorContext authenticatedActorContext;

    @Inject
    WarningRuleSettingsPort settingsPort;

    @BeforeEach
    void setUp() {
        QuarkusTransaction.requiringNew().run(() -> Arrays.stream(WarningRule.values())
                .forEach(rule -> settingsPort.save(new WarningRuleSetting(rule, true, null))));
        when(authenticatedActorContext.roles()).thenReturn(Set.of(Role.EMPLOYEE, Role.ADMIN));
    }

    @Test
    void list_shouldReturnOneEntryPerRule() {
        given().accept(ContentType.JSON).get("/worktime/warning-rules")
                .then().statusCode(200)
                .body("$", hasSize(WarningRule.values().length))
                .body("find { it.rule == 'TIME_OVERLAP' }.enabled", Matchers.is(true))
                .body("find { it.rule == 'TIME_OVERLAP' }.activeFrom", nullValue());
    }

    @Test
    void update_shouldPersistAndReturnTheEntry() {
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body("{\"enabled\": false, \"activeFrom\": \"2026-11-01\"}")
                .put("/worktime/warning-rules/MEAL_ENTRY")
                .then().statusCode(200)
                .body("rule", Matchers.is("MEAL_ENTRY"))
                .body("enabled", Matchers.is(false))
                .body("activeFrom", Matchers.is("2026-11-01"));

        assertThat(settingsPort.findAll()).contains(new WarningRuleSetting(WarningRule.MEAL_ENTRY, false, LocalDate.of(2026, 11, 1)));
    }

    @Test
    void update_shouldRemoveStartDateWhenNull() {
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body("{\"enabled\": true, \"activeFrom\": null}")
                .put("/worktime/warning-rules/BILLABLE_PROJECT")
                .then().statusCode(200)
                .body("activeFrom", nullValue());
    }

    @Test
    void update_shouldRejectUnknownRuleWith404() {
        given().contentType(ContentType.JSON).body("{\"enabled\": true}")
                .put("/worktime/warning-rules/UNKNOWN_RULE")
                .then().statusCode(404);
    }

    @Test
    void update_shouldRejectMissingEnabledWith400() {
        given().contentType(ContentType.JSON).body("{\"activeFrom\": \"2026-11-01\"}")
                .put("/worktime/warning-rules/MEAL_ENTRY")
                .then().statusCode(400);
    }

    @Test
    void update_shouldRejectMalformedActiveFromWith400() {
        given().contentType(ContentType.JSON).body("{\"enabled\": true, \"activeFrom\": \"not-a-date\"}")
                .put("/worktime/warning-rules/MEAL_ENTRY")
                .then().statusCode(400);
    }

    @Test
    void shouldRejectNonAdminWith403() {
        when(authenticatedActorContext.roles()).thenReturn(Set.of(Role.EMPLOYEE));

        given().accept(ContentType.JSON).get("/worktime/warning-rules").then().statusCode(403);
        given().contentType(ContentType.JSON).body("{\"enabled\": true}")
                .put("/worktime/warning-rules/MEAL_ENTRY").then().statusCode(403);
    }
}
