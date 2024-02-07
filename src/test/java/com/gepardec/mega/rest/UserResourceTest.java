package com.gepardec.mega.rest;

import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.rest.mapper.UserMapper;
import com.gepardec.mega.rest.model.UserDto;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
@JwtSecurity(claims = {
        @Claim(key = "email", value = "test@gepardec.com")
})
class UserResourceTest {

    @InjectMock
    private UserContext userContext;

    @InjectSpy
    private UserMapper userMapper;

    @Test
    @TestSecurity
    @JwtSecurity
    void get_whenUserNotLogged_thenReturnsHttpStatusUNAUTHORIZED() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        given().get("/user")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void get_whenUserIsLogged_thenReturnsHttpStatusOK() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        given().get("/user")
                .then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void get_whenUserIsLogged_thenReturnsUser() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);
        final UserDto actual = given()
                .get("/user")
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().as(UserDto.class);

        assertThat(actual.getUserId()).isEqualTo(user.getUserId());
        assertThat(actual.getEmail()).isEqualTo(user.getEmail());
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
}
