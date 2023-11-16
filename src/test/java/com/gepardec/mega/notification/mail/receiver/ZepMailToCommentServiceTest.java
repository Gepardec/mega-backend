package com.gepardec.mega.notification.mail.receiver;

import com.gepardec.mega.application.exception.ForbiddenException;
import com.gepardec.mega.domain.model.SourceSystem;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.notification.mail.Mail;
import com.gepardec.mega.notification.mail.MailParameter;
import com.gepardec.mega.notification.mail.MailSender;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.UserService;
import com.sun.mail.imap.IMAPMessage;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZepMailToCommentServiceTest {

    @Mock
    Logger logger;

    @Mock
    ZepProjektzeitDetailsMailMapper zepProjektzeitDetailsMailMapper;

    @Mock
    UserService userService;

    @Mock
    CommentService commentService;

    @Mock
    MailSender mailSender;

    @InjectMocks
    ZepMailToCommentService testedObject;

    @Test
    void saveAsComment_() throws MessagingException, IOException {
        //GIVEN
        when(zepProjektzeitDetailsMailMapper.convert(any())).thenReturn(of(createZepProjektzeitDetailsMail()));
        when(userService.findByName(any(), any())).thenReturn(createEmpfaenger());
        when(userService.findByZepId(any())).thenReturn(createErsteller());

        //WHEN
        testedObject.saveAsComment(mock(IMAPMessage.class));

        //THEN
        verify(commentService, times(1)).create(
                StepName.CONTROL_TIME_EVIDENCES.getId(),
                SourceSystem.ZEP,
                "max.empfaengermann@gepardec.com",
                "Deine Buchung vom 03.11.2023 im Projekt 'Gepardec' weist beim Vorgang 'Learning Friday' " +
                        "von ? bis ? (hh:mm) mit der Bemerkung 'MEGA' einen Fehler auf. Falsche Buchung!",
                "max.erstellermann@gepardec.com",
                "Gepardec",
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
        assertThatCode(() -> testedObject.saveAsComment(mock(IMAPMessage.class)))
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
        assertThatCode(() -> testedObject.saveAsComment(mock(IMAPMessage.class)))
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
                }},
                List.of("unknown")
        );
    }

    private ZepProjektzeitDetailsMail createZepProjektzeitDetailsMail() {
        return ZepProjektzeitDetailsMail.builder()
                .withTag(LocalDate.of(2023, 11, 3))
                .withZepIdErsteller("001-mempfaengermann")
                .withMitarbeiterVorname("Max")
                .withMitarbeiterNachname("Empfaengermann")
                .withNachricht("Falsche Buchung!")
                .withBuchungInfo("von ? bis ? (hh:mm)")
                .withProjekt("Gepardec")
                .withVorgang("Learning Friday")
                .withBemerkung("MEGA")
                .build();
    }

    private User createEmpfaenger() {
        return User.builder()
                .firstname("Max")
                .lastname("Empfaengermann")
                .email("max.empfaengermann@gepardec.com")
                .build();
    }

    private User createErsteller() {
        return User.builder()
                .firstname("Max")
                .lastname("Erstellermann")
                .email("max.erstellermann@gepardec.com")
                .build();
    }
}
