package com.gepardec.mega.rest;

import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.rest.model.PrematureEmployeeCheckDto;
import com.gepardec.mega.rest.model.UserDto;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
@OidcSecurity(claims = {
        @Claim(key = "email", value = "test@gepardec.com")
})
class PrematureEmployeeCheckResourceTest {

    @InjectMock
    private UserContext userContext;

    @InjectMock
    private PrematureEmployeeCheckService prematureEmployeeCheckService;


    @Test
    @TestSecurity
    @OidcSecurity
    void add_unauthorized_status401() {
//        Given
        when(userContext.getUser()).thenReturn(createUserForRole(Role.EMPLOYEE));

//        When & Then
        given().contentType(ContentType.JSON).post("/prematureemployeecheck")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void add_authorized_success() {
//        Given
        when(userContext.getUser()).thenReturn(createUserForRole(Role.EMPLOYEE));

        final UserDto user = createUserDtoForRole(Role.EMPLOYEE);
        final LocalDate localDate = LocalDate.of(2023, 10, 1);
        final PrematureEmployeeCheckDto prematureEmployeeCheckDto = PrematureEmployeeCheckDto.builder()
                .user(user)
                .forMonth(localDate)
                .build();

        when(prematureEmployeeCheckService.create(any())).thenReturn(true);

//        When
        Boolean addedPrematureEmployeeCheck = given().contentType(ContentType.JSON)
                .body(prematureEmployeeCheckDto)
                .post("/prematureemployeecheck")
                .as(Boolean.class);

//        Then
        assertThat(addedPrematureEmployeeCheck).isTrue();
    }


    private User createUserForRole(final Role role) {
        return User.builder()
                .dbId(1)
                .userId("1")
                .email("max.mustermann@gpeardec.com")
                .firstname("Max")
                .lastname("Mustermann")
                .roles(Set.of(role))
                .build();
    }

    private UserDto createUserDtoForRole(final Role role) {
        return UserDto.builder()
                .dbId(1)
                .userId("1")
                .email("max.mustermann@gpeardec.com")
                .firstname("Max")
                .lastname("Mustermann")
                .roles(Set.of(role))
                .build();
    }
}
