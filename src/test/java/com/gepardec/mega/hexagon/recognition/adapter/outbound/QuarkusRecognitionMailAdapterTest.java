package com.gepardec.mega.hexagon.recognition.adapter.outbound;

import com.gepardec.mega.hexagon.recognition.application.model.RecognitionDigestEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionCategory;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionMailRecipient;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import io.quarkus.mailer.Attachment;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class QuarkusRecognitionMailAdapterTest {

    @Inject
    QuarkusRecognitionMailAdapter adapter;

    @Inject
    MockMailbox mailbox;

    @BeforeEach
    void setUp() {
        mailbox.clear();
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
        assertThat(mail.getSubject()).isEqualTo("UNIT-TEST: MEGA Briefkasten: Wöchentliche Anerkennungen");
        assertThat(mail.getHtml()).contains("Hallo Ada,");
        assertThat(mail.getHtml()).contains("Lob &amp; Wertschätzung", "Danke für die Hilfe im Kundentermin.");
        assertThat(mail.getHtml()).contains("<h2>Mut</h2>", "Mutige Entscheidung unter Druck.");
        assertThat(mail.getHtml()).contains("Eingereicht von: Grace Hopper");
    }

    @Test
    void sendDigest_shouldRenderEmptyStateWhenNoEntriesExist() {
        adapter.sendDigest(new RecognitionMailRecipient(Email.of("lead@example.com"), "Ada"), List.of());

        assertThat(capturedMail().getHtml())
                .contains("Diese Woche wurden keine neuen Anerkennungen eingereicht.");
    }

    @Test
    void sendDigest_shouldRenderOnlyAppreciationSectionWhenNoCourageEntriesExist() {
        adapter.sendDigest(new RecognitionMailRecipient(Email.of("lead@example.com"), "Ada"), List.of(
                entry("Danke für die Hilfe im Kundentermin.", RecognitionCategory.APPRECIATION)
        ));

        assertThat(capturedMail().getHtml())
                .contains("<h2>Lob &amp; Wertschätzung</h2>", "Danke für die Hilfe im Kundentermin.")
                .doesNotContain("<h2>Mut</h2>");
    }

    @Test
    void sendDigest_shouldRenderOnlyCourageSectionWhenNoAppreciationEntriesExist() {
        adapter.sendDigest(new RecognitionMailRecipient(Email.of("lead@example.com"), "Ada"), List.of(
                entry("Mutige Entscheidung unter Druck.", RecognitionCategory.COURAGE)
        ));

        assertThat(capturedMail().getHtml())
                .contains("<h2>Mut</h2>", "Mutige Entscheidung unter Druck.")
                .doesNotContain("<h2>Lob &amp; Wertschätzung</h2>");
    }

    @Test
    void sendDigest_shouldHtmlEscapeEntryMessages() {
        String message = "<strong>Danke & willkommen</strong>";
        adapter.sendDigest(new RecognitionMailRecipient(Email.of("lead@example.com"), "Ada"), List.of(
                entry(message, RecognitionCategory.APPRECIATION)
        ));

        assertThat(capturedMail().getHtml())
                .contains("&lt;strong&gt;Danke &amp; willkommen&lt;/strong&gt;")
                .doesNotContain(message);
    }

    @Test
    void sendDigest_shouldRenderAnonymForAnonymousEntries() {
        adapter.sendDigest(new RecognitionMailRecipient(Email.of("lead@example.com"), "Ada"), List.of(
                entry("Danke für deine Hilfe.", RecognitionCategory.APPRECIATION, "Anonym")
        ));

        assertThat(capturedMail().getHtml()).contains("Eingereicht von: Anonym");
    }

    @Test
    void sendDigest_shouldIncludeInlineLogoAttachmentAndReferenceItFromTheBody() {
        adapter.sendDigest(new RecognitionMailRecipient(Email.of("lead@example.com"), "Ada"), List.of());

        Mail mail = capturedMail();
        assertThat(mail.getHtml()).contains("src=\"cid:LogoMEGAdash@gepardec.com\"");
        assertThat(mail.getAttachments()).singleElement().satisfies(attachment -> {
            assertThat(attachment.getContentId()).isEqualTo("<LogoMEGAdash@gepardec.com>");
            assertThat(attachment.getDisposition()).isEqualTo(Attachment.DISPOSITION_INLINE);
        });
    }

    private Mail capturedMail() {
        return mailbox.getMailsSentTo("lead@example.com").getFirst();
    }

    private RecognitionDigestEntry entry(String message, RecognitionCategory category) {
        return entry(message, category, "Grace Hopper");
    }

    private RecognitionDigestEntry entry(String message, RecognitionCategory category, String submitterName) {
        return new RecognitionDigestEntry(message, category, submitterName);
    }
}
