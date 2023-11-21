package com.gepardec.mega.rest;

import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.rest.model.PrematureEmployeeCheckDto;
import com.gepardec.mega.rest.model.UserDto;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
@JwtSecurity(claims = {
        @Claim(key = "email", value = "test@gepardec.com")
})
public class PrematureEmployeeCheckResourceTest {


    @InjectMock
    private UserContext userContext;

    @InjectMock
    private PrematureEmployeeCheckService prematureEmployeeCheckService;


    @Test
    @TestSecurity
    @JwtSecurity
    public void addNewPrematureEmployeeCheckUnauthorized_THEN_GET_STATUS_401() {
        when(userContext.getUser()).thenReturn(createUserForRole(Role.EMPLOYEE));

        given().contentType(ContentType.JSON).post("/prematureemployeecheck")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void addNewPrematureEmployeeCheckAuthorized_THEN_RETURN_TRUE() {
        when(userContext.getUser()).thenReturn(createUserForRole(Role.EMPLOYEE));

        final UserDto user = createUserDtoForRole(Role.EMPLOYEE);
        final LocalDate localDate = LocalDate.of(2023, 10, 1);
        final PrematureEmployeeCheckDto prematureEmployeeCheckDto = PrematureEmployeeCheckDto.builder()
                .user(user)
                .forMonth(localDate)
                .build();

        when(prematureEmployeeCheckService.addPrematureEmployeeCheck(any())).thenReturn(true);


        Boolean addedPrematureEmployeeCheck = given().contentType(ContentType.JSON)
                .body(prematureEmployeeCheckDto)
                .post("/prematureemployeecheck")
                .as(Boolean.class);

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
