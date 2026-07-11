package com.gepardec.mega.hexagon.recognition.adapter.outbound;

import com.gepardec.mega.hexagon.recognition.application.port.outbound.RecognitionMailPort;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionCategory;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntry;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionMailRecipient;
import com.google.common.html.HtmlEscapers;
import com.google.common.net.MediaType;
import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

@ApplicationScoped
public class QuarkusRecognitionMailAdapter implements RecognitionMailPort {

    private static final String TEMPLATE_PATH = "emails/recognition-digest.html";
    private static final String LOGO_RESOURCE_PATH = "img/logo.png";
    private static final String SUBJECT_KEY = "mail.RECOGNITION_DIGEST.subject";
    private static final String FIRST_NAME_PARAMETER = "$firstName$";
    private static final String ENTRIES_PARAMETER = "$entries$";
    private static final String EMPTY_STATE = "<p>Diese Woche wurden keine neuen Anerkennungen eingereicht.</p>";

    private final Mailer mailer;
    private final Optional<String> subjectPrefix;

    @Inject
    public QuarkusRecognitionMailAdapter(
            Mailer mailer,
            @ConfigProperty(name = "mega.mail.subject-prefix") Optional<String> subjectPrefix
    ) {
        this.mailer = mailer;
        this.subjectPrefix = subjectPrefix;
    }

    @Override
    public void sendDigest(RecognitionMailRecipient recipient, List<RecognitionEntry> entries) {
        String subject = subjectPrefix.orElse("") + ResourceBundle.getBundle(
                        "messages",
                        Locale.GERMAN,
                        ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
                )
                .getString(SUBJECT_KEY);
        String content = readTemplate()
                .replace(FIRST_NAME_PARAMETER, escapeHtml(recipient.firstName()))
                .replace(ENTRIES_PARAMETER, renderEntries(entries));

        mailer.send(Mail.withHtml(recipient.email().value(), subject, content)
                .addInlineAttachment("logo.png", readLogo(), MediaType.PNG.type(), "<LogoMEGAdash@gepardec.com>"));
        Log.info("Recognition digest email sent");
    }

    private String renderEntries(List<RecognitionEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return EMPTY_STATE;
        }

        String appreciationEntries = renderCategoryEntries(entries, RecognitionCategory.APPRECIATION, "Lob &amp; Wertschätzung");
        String courageEntries = renderCategoryEntries(entries, RecognitionCategory.COURAGE, "Mut");
        return appreciationEntries + courageEntries;
    }

    private String renderCategoryEntries(List<RecognitionEntry> entries, RecognitionCategory category, String heading) {
        String listItems = entries.stream()
                .filter(entry -> entry.category() == category)
                .map(entry -> "<li>" + escapeHtml(entry.message()) + "</li>")
                .reduce("", String::concat);
        if (listItems.isEmpty()) {
            return "";
        }

        return "<h2>" + heading + "</h2><ul>" + listItems + "</ul>";
    }

    private String readTemplate() {
        try (InputStream inputStream = QuarkusRecognitionMailAdapter.class.getClassLoader().getResourceAsStream(TEMPLATE_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("Could not read email template resource '%s'".formatted(TEMPLATE_PATH));
            }
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read email template resource '%s'".formatted(TEMPLATE_PATH), exception);
        }
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

    private String escapeHtml(String value) {
        return HtmlEscapers.htmlEscaper().escape(value);
    }
}
