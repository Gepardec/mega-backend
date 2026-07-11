package com.gepardec.mega.hexagon.recognition.adapter.outbound;

import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionCategory;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntryId;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionMailRecipient;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class QuarkusRecognitionMailAdapterTest {

    private Mailer mailer;
    private QuarkusRecognitionMailAdapter adapter;

    @BeforeEach
    void setUp() {
        mailer = mock(Mailer.class);
        adapter = new QuarkusRecognitionMailAdapter(mailer, Optional.of("TEST: "));
    }

    @Test
    void sendDigest_shouldRenderCategoryGroupedEntriesAndSendToRecipientAddress() {
        RecognitionMailRecipient recipient = new RecognitionMailRecipient(Email.of("lead@example.com"), "Ada");

        adapter.sendDigest(recipient, List.of(
                entry("Danke für die Hilfe im Kundentermin.", RecognitionCategory.APPRECIATION),
                entry("Mutige Entscheidung unter Druck.", RecognitionCategory.COURAGE)
        ));

        Mail mail = capturedMail();
        assertThat(mail.getTo()).containsExactly("lead@example.com");
        assertThat(mail.getSubject()).isEqualTo("TEST: MEGA Briefkasten: Wöchentliche Anerkennungen");
        assertThat(mail.getHtml()).contains("Hallo Ada,");
        assertThat(mail.getHtml()).contains("Lob &amp; Wertschätzung", "Danke für die Hilfe im Kundentermin.");
        assertThat(mail.getHtml()).contains("<h2>Mut</h2>", "Mutige Entscheidung unter Druck.");
    }

    @Test
    void sendDigest_shouldRenderEmptyStateWhenNoEntriesExist() {
        adapter.sendDigest(new RecognitionMailRecipient(Email.of("lead@example.com"), "Ada"), List.of());

        assertThat(capturedMail().getHtml())
                .contains("Diese Woche wurden keine neuen Anerkennungen eingereicht.");
    }

    private Mail capturedMail() {
        ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(mailer).send(mailCaptor.capture());
        return mailCaptor.getValue();
    }

    private RecognitionEntry entry(String message, RecognitionCategory category) {
        return RecognitionEntry.create(
                RecognitionEntryId.of(Instancio.create(UUID.class)),
                message,
                category,
                Instant.parse("2026-07-06T15:30:00Z")
        );
    }
}
