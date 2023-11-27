package com.gepardec.mega.notification.mail.receiver;

import com.sun.mail.imap.IMAPMessage;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ZepProjektzeitDetailsMailMapperTest {

    private static final String SUBJECT = "Projektzeit Fr, 03.11.2023";
    private static final String BODY_CONTENT = "von 11:15 bis 12:00 (0,75 Stunden)\n" +
            "\n" +
            "\n" +
            "\n" +
            "Nachricht: Projekt passt nicht, bitte anpassen!\n" +
            "\n" +
            "\n" +
            "\n" +
            "Ersteller-ID: 001-mmustermann\n" +
            "Mitarbeiter: Mustermann, Max\n" +
            "Projekt: Gepardec\n" +
            "Vorgang: Learning Friday\n" +
            "Ticket:\n" +
            "Teilaufgabe:\n" +
            "Tätigkeit: bearbeiten\n" +
            "Ort:\n" +
            "Bemerkungen: MEGA";

    @InjectMock
    Logger logger;

    @Inject
    ZepProjektzeitDetailsMailMapper testedObject;

    private Message givenMessage;

    @BeforeEach
    void setup() throws MessagingException, IOException {
        var bodyPart = mock(BodyPart.class);
        when(bodyPart.getContent()).thenReturn(BODY_CONTENT);

        var multipart = mock(Multipart.class);
        when(multipart.getBodyPart(0)).thenReturn(bodyPart);

        givenMessage = mock(IMAPMessage.class);
        when(givenMessage.getContent()).thenReturn(multipart);
        when(givenMessage.getSubject()).thenReturn(SUBJECT);
    }

    @Test
    void convert_ValidMessage_MappedSuccessfully() throws MessagingException, IOException {
        //GIVEN
        //default setup

        //WHEN
        var result = testedObject.convert(givenMessage);

        //THEN
        assertThat(result).isPresent();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.get().getTag()).as("Tag").isEqualTo(LocalDate.of(2023, 11, 3));
            softly.assertThat(result.get().getNachricht()).as("Nachricht").isEqualTo("Projekt passt nicht, bitte anpassen!");
            softly.assertThat(result.get().getZepIdErsteller()).as("ZepIdErsteller").isEqualTo("001-mmustermann");
            softly.assertThat(result.get().getBuchungInfo()).as("BuchungInfo").isEqualTo("von 11:15 bis 12:00 (0,75 Stunden)");
            softly.assertThat(result.get().getMitarbeiterVorname()).as("MitarbeiterVorname").isEqualTo("Max");
            softly.assertThat(result.get().getMitarbeiterNachname()).as("MitarbeiterNachname").isEqualTo("Mustermann");
            softly.assertThat(result.get().getProjekt()).as("Projekt").isEqualTo("Gepardec");
            softly.assertThat(result.get().getVorgang()).as("Vorgang").isEqualTo("Learning Friday");
            softly.assertThat(result.get().getBemerkung()).as("Bemerkung").isEqualTo("MEGA");
        });
    }

    @Test
    void convert_SubjectDoesNotMatchRegex_EmptyOptional() throws MessagingException, IOException {
        //GIVEN
        when(givenMessage.getSubject()).thenReturn("Invalid subject!");

        //WHEN
        //THEN
        assertThat(testedObject.convert(givenMessage)).isEmpty();
        verify(logger, times(1)).error(any());
    }

    @Test
    void convert_BodyIsMissingInformation_EmptyOptional() throws MessagingException, IOException {
        //GIVEN
        var bodyPart = mock(BodyPart.class);
        when(bodyPart.getContent()).thenReturn(BODY_CONTENT.lines().limit(10)); // truncate body

        var multipart = mock(Multipart.class);
        when(multipart.getBodyPart(0)).thenReturn(bodyPart);
        when(givenMessage.getContent()).thenReturn(multipart);

        //WHEN
        //THEN
        assertThat(testedObject.convert(givenMessage)).isEmpty();
        verify(logger, times(1)).error(any());
    }
}
