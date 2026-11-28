package com.gepardec.mega.rest;

import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.personio.employees.PersonioEmployeesService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
@OidcSecurity(claims = {
        @Claim(key = "email", value = "test@gepardec.com")
})
class WorkerResourceProblemTest {

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    PersonioEmployeesService personioEmployeesService;

    @Test
    void getLeaders_shouldReturnProblemWithoutCode_whenServiceFailsUnexpectedly() {
        User employee = Instancio.of(User.class)
                .set(field(User::email), Email.of("test@gepardec.com"))
                .set(field(User::roles), Set.of(Role.EMPLOYEE))
                .create();
        when(userRepository.findByEmail(Email.of("test@gepardec.com"))).thenReturn(Optional.of(employee));
        when(personioEmployeesService.getPersonioEmployeeByEmail(anyString()))
                .thenThrow(new IllegalStateException("personio unavailable for test@gepardec.com"));

        given()
                .accept(ContentType.JSON)
                .get("/worker/leaders")
                .then()
                .statusCode(500)
                .contentType("application/problem+json")
                .body("status", is(500))
                .body("title", is("Internal Server Error"))
                .body("instance", is("/worker/leaders"))
                .body("code", nullValue())
                .body("detail", nullValue())
                .body(not(containsString("test@gepardec.com")));
    }

    @Test
    void getBillInfoForEmployee_shouldReturnBadRequest_whenPayrollMonthIsMalformed() {
        User employee = Instancio.of(User.class)
                .set(field(User::email), Email.of("test@gepardec.com"))
                .set(field(User::roles), Set.of(Role.EMPLOYEE))
                .create();
        when(userRepository.findByEmail(Email.of("test@gepardec.com"))).thenReturn(Optional.of(employee));

        given()
                .accept(ContentType.JSON)
                .queryParam("payrollMonth", "2026-13")
                .get("/worker/bills")
                .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("code", nullValue());
    }
}
