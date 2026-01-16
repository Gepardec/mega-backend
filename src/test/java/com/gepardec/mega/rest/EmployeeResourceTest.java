package com.gepardec.mega.rest;

import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.rest.mapper.EmployeeMapper;
import com.gepardec.mega.rest.model.EmployeeDto;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.zep.impl.ZepServiceImpl;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.specification.MultiPartSpecification;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
@OidcSecurity(claims = {
        @Claim(key = "email", value = "test@gepardec.com")
})
class EmployeeResourceTest {

    @Inject
    EmployeeMapper mapper;


    @InjectMock
    EmployeeService employeeService;


    @InjectMock
    UserContext userContext;

    @InjectMock
    ZepServiceImpl zepService;

    @InjectMock
    UserRepository userRepo;

    @BeforeEach
    void setUp() {
        doNothing()
                .when(zepService)
                .updateEmployeeHourlyRate(
                        any(String.class),
                        any(Double.class),
                        any(String.class)
                );
    }

    private static final String VALID_HOURLY_RATES_CSV = """
            #ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD
            005-wbruckmueller,72.00,2025-01-01
            102-funger,20.00,2025-12-31
            """;

    private static final String INVALID_HOURLY_RATES_CSV = """
            #ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD
            ,,
            102-funger,20.00,2025-12-31
            """;

    private static final String NON_EXISTING_USER_HOURLY_RATES_CSV = """
            #ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD
            xxxxx,15.00,2025-01-01
            """;

    @Test
    void uploadCorrectInternalRate() {
        final User user = createUserForRole(Role.OFFICE_MANAGEMENT);
        when(userContext.getUser()).thenReturn(user);
        final EmployeeDto userAsEmployee = createEmployeeForUser(user);
        when(employeeService.getEmployee(anyString())).thenReturn(mapper.mapToDomain(userAsEmployee));

        when(userRepo.findByZepId(any(String.class)))
                .thenReturn(Optional.of(com.gepardec.mega.db.entity.employee.User.of("test@mail.com")));

        given()
                .multiPart(buildMultipartSpec(VALID_HOURLY_RATES_CSV))
                .when()
                .post("/employees/bulkUpdate")
                .then()
                .assertThat()
                .statusCode(200);

        verify(zepService).updateEmployeeHourlyRate(eq("005-wbruckmueller"), eq(72.00D), eq("2025-01-01"));
        verify(zepService).updateEmployeeHourlyRate(eq("102-funger"), eq(20.00D), eq("2025-12-31"));
    }

    @Test
    void uploadIncorrectInternalRate() {
        final User user = createUserForRole(Role.OFFICE_MANAGEMENT);
        when(userContext.getUser()).thenReturn(user);
        final EmployeeDto userAsEmployee = createEmployeeForUser(user);
        when(employeeService.getEmployee(anyString())).thenReturn(mapper.mapToDomain(userAsEmployee));

        given()
                .multiPart(buildMultipartSpec(INVALID_HOURLY_RATES_CSV))
                .when()
                .post("/employees/bulkUpdate")
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", anyOf(
                        equalTo("Error: The uploaded file is not formatted correctly"),
                        equalTo("Fehler: Die hochgeladene Datei ist nicht richtig formatiert")))
                .body("location", equalTo(List.of(2)));
    }

    @Test
    void uploadInternalrateWithNonExistingEmployee() {
        final User user = createUserForRole(Role.OFFICE_MANAGEMENT);
        when(userContext.getUser()).thenReturn(user);
        final EmployeeDto userAsEmployee = createEmployeeForUser(user);
        when(employeeService.getEmployee(anyString())).thenReturn(mapper.mapToDomain(userAsEmployee));

        when(userRepo.findByZepId(any(String.class)))
                .thenReturn(Optional.empty());

        given()
                .multiPart(buildMultipartSpec(NON_EXISTING_USER_HOURLY_RATES_CSV))
                .when()
                .post("/employees/bulkUpdate")
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", anyOf(
                        equalTo("Error: The specified employee does not exist"),
                        equalTo("Fehler: Der angegebene Mitarbeiter existiert nicht")))
                .body("location", equalTo(List.of(2)));
    }

    private MultiPartSpecification buildMultipartSpec(String fileContent) {
        return new MultiPartSpecBuilder(fileContent)
                .fileName("hourlyRates.csv")
                .controlName("file")
                .mimeType("text/plain")
                .build();
    }

    @Test
    @TestSecurity
    @OidcSecurity
    void list_whenUserNotLoggedAndInRoleOFFICE_MANAGEMENT_thenReturnsHttpStatusUNAUTHORIZED() {
        when(userContext.getUser()).thenReturn(createUserForRole(Role.OFFICE_MANAGEMENT));

        given().get("/employees")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    @TestSecurity
    @OidcSecurity
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
        final EmployeeDto actual = employees.getFirst();
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

    @Test
    void downloadCsvTemplate_whenUserLoggedAndInRoleOFFICE_MANAGEMENT_thenReturnsHttpStatusOK() {
        final User user = createUserForRole(Role.OFFICE_MANAGEMENT);
        when(userContext.getUser()).thenReturn(user);

        given().get("/employees/csvTemplate")
                .then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void downloadCsvTemplate_whenUserLoggedAndInRolePROJECT_LEAD_thenReturnsHttpStatusOK() {
        final User user = createUserForRole(Role.PROJECT_LEAD);
        when(userContext.getUser()).thenReturn(user);

        given().get("/employees/csvTemplate")
                .then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void downloadCsvTemplate_whenUserLoggedAndInRoleEMPLOYEE_thenReturnsHttpStatusFORBIDDEN() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        given().get("/employees/csvTemplate")
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    @TestSecurity
    @OidcSecurity
    void downloadCsvTemplate_whenUserNotLogged_thenReturnsHttpStatusUNAUTHORIZED() {
        given().get("/employees/csvTemplate")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void downloadCsvTemplate_whenCalled_thenReturnsCorrectCsvContent() {
        final User user = createUserForRole(Role.OFFICE_MANAGEMENT);
        when(userContext.getUser()).thenReturn(user);

        final Employee employee1 = Employee.builder()
                .userId("102-atest")
                .email("atest@gepardec.com")
                .firstname("Alpha")
                .lastname("Test")
                .build();

        final Employee employee2 = Employee.builder()
                .userId("005-btest")
                .email("btest@gepardec.com")
                .firstname("Beta")
                .lastname("Test")
                .build();

        when(employeeService.getAllActiveEmployees()).thenReturn(List.of(employee1, employee2));

        final String csvContent = given().get("/employees/csvTemplate")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header("Content-Disposition", equalTo("attachment; filename=\"hourly_rates_template.csv\""))
                .extract().asString();

        final String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        assertAll(
                () -> assertThat(csvContent).contains("#ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD"),
                () -> assertThat(csvContent).contains("005-btest,," + currentDate),
                () -> assertThat(csvContent).contains("102-atest,," + currentDate)
        );
    }

    @Test
    void downloadCsvTemplate_whenCalled_thenEmployeesSortedByUserId() {
        final User user = createUserForRole(Role.OFFICE_MANAGEMENT);
        when(userContext.getUser()).thenReturn(user);

        final Employee employee1 = Employee.builder()
                .userId("102-atest")
                .email("atest@gepardec.com")
                .firstname("Alpha")
                .lastname("Test")
                .build();

        final Employee employee2 = Employee.builder()
                .userId("005-btest")
                .email("btest@gepardec.com")
                .firstname("Beta")
                .lastname("Test")
                .build();

        final Employee employee3 = Employee.builder()
                .userId("050-ctest")
                .email("ctest@gepardec.com")
                .firstname("Gamma")
                .lastname("Test")
                .build();

        // Return employees in unsorted order
        when(employeeService.getAllActiveEmployees()).thenReturn(List.of(employee1, employee3, employee2));

        final String csvContent = given().get("/employees/csvTemplate")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().asString();

        final String[] lines = csvContent.split("\n");
        assertAll(
                () -> assertThat(lines).hasSizeGreaterThanOrEqualTo(4), // Header + 3 employees + trailing newline
                () -> assertThat(lines[1]).startsWith("005-btest"), // First employee by ID
                () -> assertThat(lines[2]).startsWith("050-ctest"), // Second employee by ID
                () -> assertThat(lines[3]).startsWith("102-atest") // Third employee by ID
        );
    }

    @Test
    void downloadCsvTemplate_whenNoEmployees_thenReturnsOnlyHeader() {
        final User user = createUserForRole(Role.OFFICE_MANAGEMENT);
        when(userContext.getUser()).thenReturn(user);

        when(employeeService.getAllActiveEmployees()).thenReturn(List.of());

        final String csvContent = given().get("/employees/csvTemplate")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().asString();

        assertThat(csvContent).isEqualTo("#ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD\n\n");
    }

    private EmployeeDto createEmployeeForUser(final User user) {
        return EmployeeDto.builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .title("Ing.")
                .userId(user.getUserId())
                .releaseDate("2020-01-01")
                .active(false)
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
