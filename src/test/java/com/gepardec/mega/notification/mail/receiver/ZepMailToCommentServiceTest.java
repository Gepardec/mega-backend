package com.gepardec.mega.notification.mail.receiver;

import com.gepardec.mega.application.exception.ForbiddenException;
import com.gepardec.mega.domain.model.BillabilityPreset;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.SourceSystem;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.notification.mail.Mail;
import com.gepardec.mega.notification.mail.MailParameter;
import com.gepardec.mega.notification.mail.MailSender;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.ProjectService;
import com.gepardec.mega.service.api.UserService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ZepMailToCommentServiceTest {

    @InjectMock
    Logger logger;

    @InjectMock
    ZepProjektzeitDetailsMailMapper zepProjektzeitDetailsMailMapper;

    @InjectMock
    UserService userService;

    @InjectMock
    ProjectService projectService;

    @InjectMock
    CommentService commentService;

    @InjectMock
    MailSender mailSender;

    @Inject
    ZepMailToCommentService testedObject;

    @Test
    void saveAsComment_BillableProject_Successful() throws MessagingException, IOException {
        //GIVEN
        when(zepProjektzeitDetailsMailMapper.convert(any())).thenReturn(of(createZepProjektzeitDetailsMail()));
        when(userService.findByName(any(), any())).thenReturn(createEmpfaenger());
        when(userService.findByZepId(any())).thenReturn(createErsteller());
        when(projectService.getProjectByName(any(), any())).thenReturn(
                Optional.of(
                        createValidProjectBuilder()
                                .billabilityPreset(BillabilityPreset.BILLABLE)
                                .build()
                )
        );

        //WHEN
        testedObject.saveAsComment(mock(Message.class));

        //THEN
        verify(commentService, times(1)).create(
                StepName.CONTROL_TIME_EVIDENCES.getId(),
                SourceSystem.ZEP,
                "max.empfaengermann@gepardec.com",
                "Buchung vom 03.11.2023 (11:15 - 12:00) im Projekt 'Gepardec' - 'Learning Friday' " +
                        "mit dem Text 'MEGA' ist anzupassen.\n Falsche Buchung!",
                "max.erstellermann@gepardec.com",
                "Gepardec",
                "2023-11-03"

        );
        verify(mailSender, times(0)).send(any(), any(), any(), any(), any(), any());
    }

    @Test
    void saveAsComment_NotBillableProject_Successful() throws MessagingException, IOException {
        //GIVEN
        when(zepProjektzeitDetailsMailMapper.convert(any())).thenReturn(of(createZepProjektzeitDetailsMail()));
        when(userService.findByName(any(), any())).thenReturn(createEmpfaenger());
        when(userService.findByZepId(any())).thenReturn(createErsteller());
        when(projectService.getProjectByName(any(), any())).thenReturn(
                of(
                        createValidProjectBuilder()
                                .billabilityPreset(BillabilityPreset.NOT_BILLABLE)
                                .build()
                )
        );

        //WHEN
        testedObject.saveAsComment(mock(Message.class));

        //THEN
        verify(commentService, times(1)).create(
                StepName.CONTROL_INTERNAL_TIMES.getId(),
                SourceSystem.ZEP,
                "max.empfaengermann@gepardec.com",
                "Buchung vom 03.11.2023 (11:15 - 12:00) im Projekt 'Gepardec' - 'Learning Friday' " +
                        "mit dem Text 'MEGA' ist anzupassen.\n Falsche Buchung!",
                "max.erstellermann@gepardec.com",
                null,
                "2023-11-03"

        );
        verify(mailSender, times(0)).send(any(), any(), any(), any(), any(), any());
    }

    @Test
    void saveAsComment_ExceptionInMapper_ErrorLogged() throws MessagingException, IOException {
        //GIVEN
        when(zepProjektzeitDetailsMailMapper.convert(any())).thenThrow(MessagingException.class);


        //WHEN
        //THEN
        assertThatCode(() -> testedObject.saveAsComment(mock(Message.class)))
                .doesNotThrowAnyException();
        verify(logger).error(any());
        verify(commentService, times(0)).create(any(), any(), any(), any(), any(), any(), any());
        verify(mailSender, times(0)).send(any(), any(), any(), any(), any(), any());
        verify(logger).error("Recipient unknown, error cannot be reported.");
    }

    @Test
    void saveAsComment_ExceptionInMapper_EmailSent() throws MessagingException, IOException {
        //GIVEN
        when(zepProjektzeitDetailsMailMapper.convert(any())).thenReturn(of(createZepProjektzeitDetailsMail()));
        when(userService.findByZepId(any())).thenReturn(createErsteller());
        when(userService.findByName(any(), any())).thenThrow(ForbiddenException.class);

        //WHEN
        //THEN
        assertThatCode(() -> testedObject.saveAsComment(mock(Message.class)))
                .doesNotThrowAnyException();
        verify(commentService, times(0)).create(any(), any(), any(), any(), any(), any(), any());
        verify(mailSender, times(1)).send(
                Mail.ZEP_COMMENT_PROCESSING_ERROR,
                "max.erstellermann@gepardec.com",
                "unknown",
                Locale.GERMAN,
                new HashMap<>() {{
                    put(MailParameter.RECIPIENT, "Max"); // employee who sent the comment
                    put(MailParameter.COMMENT, null); // error message
                    put(MailParameter.ORIGINAL_MAIL, "Subject: Test\nBody: Content"); // original E-Mail
                }},
                List.of("unknown")
        );
    }

    private static ZepProjektzeitDetailsMail createZepProjektzeitDetailsMail() {
        return ZepProjektzeitDetailsMail.builder()
                .withTag(LocalDate.of(2023, 11, 3))
                .withUhrzeitVon("11:15")
                .withUhrzeitBis("12:00")
                .withZepIdErsteller("001-mempfaengermann")
                .withMitarbeiterVorname("Max")
                .withMitarbeiterNachname("Empfaengermann")
                .withNachricht("Falsche Buchung!")
                .withProjekt("Gepardec")
                .withVorgang("Learning Friday")
                .withBemerkung("MEGA")
                .withRawContent("Subject: Test\nBody: Content")
                .build();
    }

    private static User createEmpfaenger() {
        return User.builder()
                .firstname("Max")
                .lastname("Empfaengermann")
                .email("max.empfaengermann@gepardec.com")
                .build();
    }

    private static User createErsteller() {
        return User.builder()
                .firstname("Max")
                .lastname("Erstellermann")
                .email("max.erstellermann@gepardec.com")
                .build();
    }

    private static Project.Builder createValidProjectBuilder() {
        return Project.builder().projectId("Gepardec");
    }
}
