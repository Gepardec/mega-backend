package com.gepardec.mega.rest;

import com.gepardec.mega.db.entity.State;
import com.gepardec.mega.db.repository.CommentRepository;
import com.gepardec.mega.domain.mapper.CommentMapper;
import com.gepardec.mega.domain.model.*;
import com.gepardec.mega.notification.mail.MailSender;
import com.gepardec.mega.rest.model.OfficeManagementEntry;
import com.gepardec.mega.service.api.comment.CommentService;
import com.gepardec.mega.service.api.employee.EmployeeService;
import com.gepardec.mega.service.api.stepentry.StepEntryService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
public class EmployeeResourceTest {

    @InjectMock
    EmployeeService employeeService;

    @InjectMocks
    @Mock
    StepEntryService stepEntryService;


    @Mock
    CommentRepository commentRepository;

    @Mock
    CommentMapper commentMapper;

    @Mock
    MailSender mailSender;


    @InjectMocks
    CommentService commentService;

    @InjectMock
    private SecurityContext securityContext;

    @InjectMock
    private UserContext userContext;

    @Test
    void list_whenUserNotLoggedAndInRoleADMINISTRATOR_thenReturnsHttpStatusUNAUTHORIZED() {
        when(userContext.user()).thenReturn(createUserForRole(Role.ADMINISTRATOR));

        given().get("/employees")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void list_whenUserLoggedAndInRoleUSER_thenReturnsHttpStatusFORBIDDEN() {
        final User user = createUserForRole(Role.USER);
        when(securityContext.email()).thenReturn(user.email());
        when(userContext.user()).thenReturn(user);

        given().get("/employees")
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void list_whenUserLoggedAndInRoleCONTROLLER_thenReturnsHttpStatusOK() {
        final User user = createUserForRole(Role.CONTROLLER);
        when(securityContext.email()).thenReturn(user.email());
        when(userContext.user()).thenReturn(user);
        final Employee userAsEmployee = createEmployeeForUser(user);
        when(employeeService.getEmployee(anyString())).thenReturn(userAsEmployee);

        given().get("/employees")
                .then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void list_whenUserLoggedAndInRoleADMINISTRATOR_thenReturnsHttpStatusOK() {
        final User user = createUserForRole(Role.ADMINISTRATOR);
        when(securityContext.email()).thenReturn(user.email());
        when(userContext.user()).thenReturn(user);

        given().get("/employees")
                .then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void list_whenUserLoggedAndInRoleADMINISTRATOR_thenReturnsEmployees() {
        final User user = createUserForRole(Role.ADMINISTRATOR);
        when(securityContext.email()).thenReturn(user.email());
        when(userContext.user()).thenReturn(user);
        final Employee userAsEmployee = createEmployeeForUser(user);
        when(employeeService.getAllActiveEmployees()).thenReturn(List.of(userAsEmployee));

        final List<Employee> employees = given().get("/employees").as(new TypeRef<>() {
        });

        assertEquals(1, employees.size());
        final Employee actual = employees.get(0);
        assertEquals(userAsEmployee, actual);
    }

    @Test
    void update_whenContentTypeNotSet_returnsHttpStatusUNSUPPORTED_MEDIA_TYPE() {
        given().put("/employees")
                .then().statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void update_whenContentTypeIsTextPlain_returnsHttpStatusUNSUPPORTED_MEDIA_TYPE() {
        final User user = createUserForRole(Role.ADMINISTRATOR);
        when(userContext.user()).thenReturn(user);

        given().contentType(MediaType.TEXT_PLAIN)
                .put("/employees")
                .then().statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void update_whenEmptyBody_returnsHttpStatusBAD_REQUEST() {
        final User user = createUserForRole(Role.ADMINISTRATOR);
        when(securityContext.email()).thenReturn(user.email());
        when(userContext.user()).thenReturn(user);

        given().contentType(MediaType.APPLICATION_JSON)
                .put("/employees")
                .then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void update_whenEmptyList_returnsHttpStatusBAD_REQUEST() {
        final User user = createUserForRole(Role.ADMINISTRATOR);
        when(securityContext.email()).thenReturn(user.email());
        when(userContext.user()).thenReturn(user);

        given().contentType(MediaType.APPLICATION_JSON)
                .body(List.of())
                .put("/employees")
                .then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void update_whenValidRequest_returnsHttpStatusOK() {
        final User user = createUserForRole(Role.ADMINISTRATOR);
        when(securityContext.email()).thenReturn(user.email());
        when(userContext.user()).thenReturn(user);
        final Employee employee = createEmployeeForUser(user);

        given().contentType(MediaType.APPLICATION_JSON)
                .body(List.of(employee))
                .put("/employees")
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void update_whenValidRequestAndEmployeeServiceReturnsInvalidEmails_returnsInvalidEmails() {
        final User user = createUserForRole(Role.ADMINISTRATOR);
        when(securityContext.email()).thenReturn(user.email());
        when(userContext.user()).thenReturn(user);
        final Employee userAsEmployee = createEmployeeForUser(user);
        final List<String> expected = List.of("invalid1@gmail.com", "invalid2@gmail.com");
        when(employeeService.updateEmployeesReleaseDate(anyList())).thenReturn(expected);

        final List<String> emails = given().contentType(MediaType.APPLICATION_JSON)
                .body(List.of(userAsEmployee))
                .put("/employees")
                .as(new TypeRef<>() {
                });

        assertEquals(2, emails.size());
        assertTrue(emails.containsAll(expected));
    }

    @Test
    void noMethod_whenHttpMethodIsPOST_returns405() {
        given().post("/employees")
                .then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void getAllOfficeManagementEntries_whenNotLoggedIn_thenReturnsHttpStatusUNAUTHORIZED() {
        given().contentType(ContentType.JSON)
                .delete("/employees/officemanagemententries")
                .then().assertThat().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void getAllOfficeManagementEntries_whenValid_thenReturnsListOfEntries() {
        final User user = createUserForRole(Role.USER);
        when(securityContext.email()).thenReturn(user.email());
        when(userContext.user()).thenReturn(user);

        when(employeeService.getAllActiveEmployees())
                .thenReturn(List.of(Employee.builder().releaseDate("2020-01-01").email("marko.gattringer@gepardec.com").build()));


        List<com.gepardec.mega.db.entity.StepEntry> entries = List.of(
                createStepEntryForStep(StepName.CONTROL_EXTERNAL_TIMES, State.DONE),
                createStepEntryForStep(StepName.CONTROL_INTERNAL_TIMES, State.OPEN),
                createStepEntryForStep(StepName.CONTROL_TIME_EVIDENCES, State.DONE),
                createStepEntryForStep(StepName.CONTROL_TIMES, State.OPEN)
        );

        when(commentService.cntFinishedAndTotalCommentsForEmployee(ArgumentMatchers.any(Employee.class)))
                .thenReturn(FinishedAndTotalComments.builder().finishedComments(2L).totalComments(3L).build());


        when(stepEntryService.findAllStepEntriesForEmployee(ArgumentMatchers.any(Employee.class)))
                .thenReturn(entries);


        List<OfficeManagementEntry> result = given().contentType(ContentType.JSON)
                .get("/employees/officemanagemententries")
                .as(new TypeRef<>() {});

        assertEquals(1L, result.size());
    }

    private com.gepardec.mega.db.entity.Step createStep(StepName stepName) {
        com.gepardec.mega.db.entity.Step step = new com.gepardec.mega.db.entity.Step();
        step.setName(stepName.name());
        return step;
    }

    private com.gepardec.mega.db.entity.StepEntry createStepEntryForStep(StepName stepName, State state) {
        com.gepardec.mega.db.entity.StepEntry stepEntry = new com.gepardec.mega.db.entity.StepEntry();
        stepEntry.setStep(createStep(stepName));
        stepEntry.setState(state);
        return stepEntry;
    }

    private Employee createEmployeeForUser(final User user) {
        return Employee.builder()
                .email(user.email())
                .firstName(user.firstname())
                .sureName(user.lastname())
                .title("Ing.")
                .userId(user.userId())
                .releaseDate("2020-01-01")
                .role(user.role().roleId)
                .active(true)
                .build();
    }

    private User createUserForRole(final Role role) {
        return User.builder()
                .dbId(1)
                .userId("1")
                .email("thomas.herzog@gpeardec.com")
                .firstname("Thomas")
                .lastname("Herzog")
                .role(role)
                .build();
    }
}
