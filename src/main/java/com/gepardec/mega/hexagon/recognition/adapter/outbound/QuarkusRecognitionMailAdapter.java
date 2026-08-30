package com.gepardec.mega.hexagon.recognition.adapter.outbound;

import com.gepardec.mega.hexagon.recognition.application.port.outbound.RecognitionMailPort;
import com.gepardec.mega.hexagon.recognition.application.model.RecognitionDigestEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionCategory;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionMailRecipient;
import com.google.common.net.MediaType;
import io.quarkus.logging.Log;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

@ApplicationScoped
public class QuarkusRecognitionMailAdapter implements RecognitionMailPort {

    private static final String LOGO_RESOURCE_PATH = "img/logo.png";
    private static final String SUBJECT_KEY = "mail.RECOGNITION_DIGEST.subject";

    private final Optional<String> subjectPrefix;

    @Inject
    public QuarkusRecognitionMailAdapter(
            @ConfigProperty(name = "mega.mail.subject-prefix") Optional<String> subjectPrefix
    ) {
        this.subjectPrefix = subjectPrefix;
    }

    @Override
    public void sendDigest(RecognitionMailRecipient recipient, List<RecognitionDigestEntry> entries) {
        String subject = subjectPrefix.orElse("") + ResourceBundle.getBundle(
                        "messages",
                        Locale.GERMAN,
                        ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
                )
                .getString(SUBJECT_KEY);
        List<RecognitionDigestEntry> digestEntries = entries == null ? List.of() : entries;
        List<RecognitionDigestEntry> appreciationEntries = digestEntries.stream()
                .filter(entry -> entry.category() == RecognitionCategory.APPRECIATION)
                .toList();
        List<RecognitionDigestEntry> courageEntries = digestEntries.stream()
                .filter(entry -> entry.category() == RecognitionCategory.COURAGE)
                .toList();

        Templates.recognitionDigest(
                        recipient.firstName(),
                        digestEntries.isEmpty(),
                        appreciationEntries,
                        courageEntries
                )
                .to(recipient.email().value())
                .subject(subject)
                .addInlineAttachment("logo.png", readLogo(), MediaType.PNG.type(), "<LogoMEGAdash@gepardec.com>")
                .sendAndAwait();
        Log.info("Recognition digest email sent");
    }

    private byte[] readLogo() {
        try (InputStream inputStream = QuarkusRecognitionMailAdapter.class.getClassLoader().getResourceAsStream(LOGO_RESOURCE_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("Could not read logo resource '%s'".formatted(LOGO_RESOURCE_PATH));
            }
            return IOUtils.toByteArray(inputStream);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read logo resource '%s'".formatted(LOGO_RESOURCE_PATH), exception);
        }
    }

    @CheckedTemplate(basePath = "", defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)
    static class Templates {
        private Templates() {
        }

        static native MailTemplate.MailTemplateInstance recognitionDigest(
                String recipientFirstName,
                boolean hasNoEntries,
                List<RecognitionDigestEntry> appreciationEntries,
                List<RecognitionDigestEntry> courageEntries
        );
    }
}
