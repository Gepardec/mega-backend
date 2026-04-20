package com.gepardec.mega.hexagon.notification.adapter.outbound;

import com.gepardec.mega.hexagon.notification.domain.ClarificationNotificationType;
import com.gepardec.mega.hexagon.notification.domain.MailNotificationId;
import com.gepardec.mega.hexagon.notification.domain.ReminderType;
import com.gepardec.mega.hexagon.notification.domain.port.outbound.NotificationMailPort;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
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
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

@ApplicationScoped
public class QuarkusMailNotificationAdapter implements NotificationMailPort {

    private static final String EMAILS_PATH = "emails/";
    private static final String LOGO_RESOURCE_PATH = "img/logo.png";
    private static final String REMINDER_TEMPLATE_PATH = EMAILS_PATH + "reminder-template.html";
    private static final String MESSAGE_KEY_TEMPLATE = "mail.%s.subject";
    private static final String PARAM_COMMENT = "$comment$";
    private static final String PARAM_FIRST_NAME = "$firstName$";
    private static final String PARAM_MAIL_TEXT = "$mailText$";
    private static final String PARAM_MEGA_DASH = "$megaDash$";
    private static final String PARAM_WIKI_EOM_URL = "$wikiEomUrl$";
    private static final String NEW_LINE_HTML = "<br>";
    private static final String NEW_LINE_STRING = "\n";

    private final Mailer mailer;
    private final Optional<String> subjectPrefix;
    private final String megaWikiEomUrl;
    private final String megaDashUrl;

    @Inject
    public QuarkusMailNotificationAdapter(
            Mailer mailer,
            @ConfigProperty(name = "mega.mail.subject-prefix") Optional<String> subjectPrefix,
            @ConfigProperty(name = "mega.wiki.eom-url") String megaWikiEomUrl,
            @ConfigProperty(name = "mega.dash-url") String megaDashUrl
    ) {
        this.mailer = mailer;
        this.subjectPrefix = subjectPrefix;
        this.megaWikiEomUrl = megaWikiEomUrl;
        this.megaDashUrl = megaDashUrl;
    }

    @Override
    public void send(
            MailNotificationId mailId,
            Email recipientEmail,
            String recipientFirstName,
            Locale locale,
            Map<String, String> templateParameters,
            List<String> subjectParameters
    ) {
        Objects.requireNonNull(mailId, "mailId must not be null");
        Objects.requireNonNull(recipientEmail, "recipientEmail must not be null");
        Objects.requireNonNull(recipientFirstName, "recipientFirstName must not be null");
        Objects.requireNonNull(locale, "locale must not be null");

        String subject = resolveSubject(mailId, locale, subjectParameters);
        String renderedContent = renderContent(mailId, recipientFirstName, locale, templateParameters);

        mailer.send(Mail.withHtml(recipientEmail.value(), subject, renderedContent)
                .addInlineAttachment("logo.png", readLogo(), MediaType.PNG.type(), "<LogoMEGAdash@gepardec.com>"));
        Log.infof("Notification %s sent to %s.", mailId.name(), recipientEmail.value());
    }

    private String resolveSubject(MailNotificationId mailId, Locale locale, List<String> subjectParameters) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                "messages",
                locale,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
        );
        String subject = subjectPrefix.orElse("")
                + resourceBundle.getString(MESSAGE_KEY_TEMPLATE.formatted(mailId.name()));

        if (subjectParameters == null || subjectParameters.isEmpty()) {
            return subject;
        }

        return MessageFormat.format(subject, subjectParameters.toArray());
    }

    private String renderContent(
            MailNotificationId mailId,
            String recipientFirstName,
            Locale locale,
            Map<String, String> templateParameters
    ) {
        Map<String, String> parameters = prepareTemplateParameters(recipientFirstName, templateParameters);
        if (mailId instanceof ReminderType reminderType) {
            return renderReminder(reminderType, locale, parameters);
        }
        if (mailId instanceof ClarificationNotificationType clarificationNotificationType) {
            return renderTemplate(resolveTemplatePath(clarificationNotificationType.name() + ".html", locale), parameters);
        }

        throw new IllegalArgumentException("Unsupported mail notification id type: " + mailId.getClass().getName());
    }

    private Map<String, String> prepareTemplateParameters(
            String recipientFirstName,
            Map<String, String> templateParameters
    ) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PARAM_FIRST_NAME, recipientFirstName);
        parameters.put(PARAM_MEGA_DASH, megaDashUrl);
        parameters.put(PARAM_WIKI_EOM_URL, megaWikiEomUrl);
        if (templateParameters != null) {
            parameters.putAll(templateParameters);
        }
        parameters.computeIfPresent(PARAM_COMMENT, (key, value) -> value.replace(NEW_LINE_STRING, NEW_LINE_HTML));
        return parameters;
    }

    private String renderReminder(ReminderType reminderType, Locale locale, Map<String, String> parameters) {
        String reminderSnippet = renderTemplate(resolveTemplatePath(reminderType.name() + ".html", locale), parameters);
        Map<String, String> wrapperParameters = new HashMap<>(parameters);
        wrapperParameters.put(PARAM_MAIL_TEXT, reminderSnippet);
        return renderTemplate(REMINDER_TEMPLATE_PATH, wrapperParameters);
    }

    private String renderTemplate(String resourcePath, Map<String, String> parameters) {
        String renderedTemplate = readTextResource(resourcePath);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            renderedTemplate = renderedTemplate.replace(entry.getKey(), Objects.toString(entry.getValue(), ""));
        }
        return renderedTemplate;
    }

    private String resolveTemplatePath(String fileName, Locale locale) {
        String localizedPath = localizedTemplatePathOrNull(fileName, locale);
        if (localizedPath != null) {
            return localizedPath;
        }

        String defaultPath = EMAILS_PATH + fileName;
        if (QuarkusMailNotificationAdapter.class.getClassLoader().getResource(defaultPath) == null) {
            throw new IllegalArgumentException("No email template for '%s' found".formatted(fileName));
        }
        return defaultPath;
    }

    private String localizedTemplatePathOrNull(String fileName, Locale locale) {
        if (locale.getLanguage().isEmpty()) {
            return null;
        }

        String localizedPath = EMAILS_PATH + locale.getLanguage().toLowerCase(Locale.ROOT) + "/" + fileName;
        if (QuarkusMailNotificationAdapter.class.getClassLoader().getResource(localizedPath) != null) {
            return localizedPath;
        }
        return null;
    }

    private String readTextResource(String resourcePath) {
        try (InputStream inputStream = QuarkusMailNotificationAdapter.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Could not read email template resource '%s'".formatted(resourcePath));
            }
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read email template resource '%s'".formatted(resourcePath), exception);
        }
    }

    private byte[] readLogo() {
        try (InputStream inputStream = QuarkusMailNotificationAdapter.class.getClassLoader().getResourceAsStream(LOGO_RESOURCE_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("Could not read logo resource '%s'".formatted(LOGO_RESOURCE_PATH));
            }
            return IOUtils.toByteArray(inputStream);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read logo resource '%s'".formatted(LOGO_RESOURCE_PATH), exception);
        }
    }
}
