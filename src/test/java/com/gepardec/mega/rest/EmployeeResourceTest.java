package com.gepardec.mega.rest;

import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.rest.mapper.EmployeeMapper;
import com.gepardec.mega.rest.model.EmployeeDto;
import com.gepardec.mega.service.api.EmployeeService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
@JwtSecurity(claims = {
        @Claim(key = "email", value = "test@gepardec.com")
})
class EmployeeResourceTest {

    @Inject
    EmployeeMapper mapper;

    @InjectMock
    EmployeeService employeeService;


    @InjectMock
    private UserContext userContext;

    @Test
    @TestSecurity
    @JwtSecurity
    void list_whenUserNotLoggedAndInRoleOFFICE_MANAGEMENT_thenReturnsHttpStatusUNAUTHORIZED() {
        when(userContext.getUser()).thenReturn(createUserForRole(Role.OFFICE_MANAGEMENT));

        given().get("/employees")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    @TestSecurity
    @JwtSecurity
    void list_whenUserNotLoggedAndInRolePROJECT_LEAD_thenReturnsHttpStatusUNAUTHORIZED() {
        when(userContext.getUser()).thenReturn(createUserForRole(Role.PROJECT_LEAD));

        given().get("/employees")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void list_whenUserLoggedAndInRoleEMPLOYEE_thenReturnsHttpStatusFORBIDDEN() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        given().get("/employees")
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void list_whenUserLoggedAndInRoleOFFICE_MANAGEMENT_thenReturnsHttpStatusOK() {
        final User user = createUserForRole(Role.OFFICE_MANAGEMENT);
        when(userContext.getUser()).thenReturn(user);
        final EmployeeDto userAsEmployee = createEmployeeForUser(user);
        when(employeeService.getEmployee(anyString())).thenReturn(mapper.mapToDomain(userAsEmployee));

        given().get("/employees")
                .then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void list_whenUserLoggedAndInRolePROJECT_LEAD_thenReturnsHttpStatusOK() {
        final User user = createUserForRole(Role.PROJECT_LEAD);
        when(userContext.getUser()).thenReturn(user);

        given().get("/employees")
                .then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void list_whenUserLoggedAndInRoleOFFICE_MANAGEMENT_thenReturnsEmployees() {
        final User user = createUserForRole(Role.OFFICE_MANAGEMENT);
        when(userContext.getUser()).thenReturn(user);
        final EmployeeDto userAsEmployee = createEmployeeForUser(user);
        when(employeeService.getAllActiveEmployees()).thenReturn(List.of(mapper.mapToDomain(userAsEmployee)));

        final List<EmployeeDto> employees = given().get("/employees").as(new TypeRef<>() {

        });

        assertThat(employees).hasSize(1);
        final EmployeeDto actual = employees.get(0);
        assertThat(actual).isEqualTo(userAsEmployee);
    }

    @Test
    void update_whenContentTypeNotSet_returnsHttpStatusUNSUPPORTED_MEDIA_TYPE() {
        given().put("/employees")
                .then().statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void update_whenContentTypeIsTextPlain_returnsHttpStatusUNSUPPORTED_MEDIA_TYPE() {
        final User user = createUserForRole(Role.PROJECT_LEAD);
        when(userContext.getUser()).thenReturn(user);

        given().contentType(MediaType.TEXT_PLAIN)
                .put("/employees")
                .then().statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void update_whenEmptyBody_returnsHttpStatusBAD_REQUEST() {
        final User user = createUserForRole(Role.PROJECT_LEAD);
        when(userContext.getUser()).thenReturn(user);

        given().contentType(MediaType.APPLICATION_JSON)
                .put("/employees")
                .then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void update_whenEmptyList_returnsHttpStatusBAD_REQUEST() {
        final User user = createUserForRole(Role.PROJECT_LEAD);
        when(userContext.getUser()).thenReturn(user);

        given().contentType(MediaType.APPLICATION_JSON)
                .body(List.of())
                .put("/employees")
                .then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void update_whenValidRequest_returnsHttpStatusOK() {
        final User user = createUserForRole(Role.PROJECT_LEAD);
        when(userContext.getUser()).thenReturn(user);
        final EmployeeDto employee = createEmployeeForUser(user);

        given().contentType(MediaType.APPLICATION_JSON)
                .body(List.of(employee))
                .put("/employees")
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void update_whenValidRequestAndEmployeeServiceReturnsInvalidEmails_returnsInvalidEmails() {
        final User user = createUserForRole(Role.PROJECT_LEAD);
        when(userContext.getUser()).thenReturn(user);
        final EmployeeDto userAsEmployee = createEmployeeForUser(user);
        final List<String> expected = List.of("invalid1@gmail.com", "invalid2@gmail.com");
        when(employeeService.updateEmployeesReleaseDate(anyList())).thenReturn(expected);

        final List<String> emails = given().contentType(MediaType.APPLICATION_JSON)
                .body(List.of(userAsEmployee))
                .put("/employees")
                .as(new TypeRef<>() {

                });

        assertAll(
                () -> assertThat(emails).hasSize(2),
                () -> assertThat(emails).containsAll(expected)
        );
    }

    @Test
    void noMethod_whenHttpMethodIsPOST_returns405() {
        given().post("/employees")
                .then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    private EmployeeDto createEmployeeForUser(final User user) {
        return EmployeeDto.builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .title("Ing.")
                .userId(user.getUserId())
                .releaseDate("2020-01-01")
                .active(true)
                .build();
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
