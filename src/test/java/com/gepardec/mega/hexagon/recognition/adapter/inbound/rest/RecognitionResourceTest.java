package com.gepardec.mega.hexagon.recognition.adapter.inbound.rest;

import com.gepardec.mega.hexagon.recognition.application.port.inbound.SubmitRecognitionEntryUseCase;
import com.gepardec.mega.hexagon.recognition.application.port.inbound.SubmitRecognitionEntryCommand;
import com.gepardec.mega.hexagon.recognition.domain.error.RecognitionValidationException;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionCategory;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.model.User;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
class RecognitionResourceTest {

    @InjectMock
    AuthenticatedActorContext authenticatedActorContext;

    @InjectMock
    SubmitRecognitionEntryUseCase submitRecognitionEntryUseCase;

    private UserId submitterId;

    @BeforeEach
    void setUp() {
        submitterId = UserId.of(Instancio.create(UUID.class));
        allowRoles(Role.EMPLOYEE);
        stubInternalUser();
        when(authenticatedActorContext.userId()).thenReturn(submitterId);
    }

    @Test
    void submitRecognitionEntry_shouldSubmitValidEntryForEmployeeRole() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"message":"Danke für deine Hilfe.","category":"APPRECIATION"}
                        """)
                .post("/recognition/entries")
                .then()
                .statusCode(201);

        verify(submitRecognitionEntryUseCase).submit(argThat(command ->
                command.message().equals("Danke für deine Hilfe.")
                        && command.category() == RecognitionCategory.APPRECIATION
                        && !command.anonymous()),
                eq(submitterId));
    }

    @Test
    void submitRecognitionEntry_shouldForwardAnonymousSubmission() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"message":"Danke für deine Hilfe.","category":"APPRECIATION","anonymous":true}
                        """)
                .post("/recognition/entries")
                .then()
                .statusCode(201);

        verify(submitRecognitionEntryUseCase).submit(argThat(SubmitRecognitionEntryCommand::anonymous), eq(submitterId));
    }

    @Test
    void submitRecognitionEntry_shouldRejectBlankMessage() {
        doThrow(new RecognitionValidationException("message must not be blank"))
                .when(submitRecognitionEntryUseCase)
                .submit(argThat(command -> command.message().isBlank()), eq(submitterId));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"message":"   ","category":"COURAGE"}
                        """)
                .post("/recognition/entries")
                .then()
                .statusCode(400);

        verify(submitRecognitionEntryUseCase).submit(argThat(command -> command.message().isBlank()), eq(submitterId));
    }

    @Test
    void submitRecognitionEntry_shouldRejectInvalidCategory() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"message":"Danke für deine Hilfe.","category":"UNKNOWN"}
                        """)
                .post("/recognition/entries")
                .then()
                .statusCode(400);

        verifyNoInteractions(submitRecognitionEntryUseCase);
    }

    @Test
    void submitRecognitionEntry_shouldRejectCallerWithoutEmployeeRole() {
        allowRoles(Role.PROJECT_LEAD);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"message":"Danke für deine Hilfe.","category":"APPRECIATION"}
                        """)
                .post("/recognition/entries")
                .then()
                .statusCode(403);

        verifyNoInteractions(submitRecognitionEntryUseCase);
    }

    @Test
    void submitRecognitionEntry_shouldRejectExternalCaller() {
        stubExternalUser();

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"message":"Danke für deine Hilfe.","category":"APPRECIATION"}
                        """)
                .post("/recognition/entries")
                .then()
                .statusCode(403);

        verifyNoInteractions(submitRecognitionEntryUseCase);
    }

    @Test
    @TestSecurity
    void submitRecognitionEntry_shouldRejectUnauthenticatedCaller() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"message":"Danke für deine Hilfe.","category":"APPRECIATION"}
                        """)
                .post("/recognition/entries")
                .then()
                .statusCode(401);

        verifyNoInteractions(submitRecognitionEntryUseCase);
    }

    private void allowRoles(Role... roles) {
        when(authenticatedActorContext.roles()).thenReturn(Set.of(roles));
    }

    private void stubInternalUser() {
        stubUser("test.internal");
    }

    private void stubExternalUser() {
        stubUser("e.external");
    }

    private void stubUser(String zepUsername) {
        User user = Instancio.of(User.class)
                .set(field(User::id), submitterId)
                .set(field(User::zepUsername), ZepUsername.of(zepUsername))
                .create();
        when(authenticatedActorContext.user()).thenReturn(user);
    }
}
