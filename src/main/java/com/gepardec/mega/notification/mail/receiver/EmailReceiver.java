package com.gepardec.mega.notification.mail.receiver;

import com.gepardec.mega.application.configuration.MailReceiverConfig;
import com.gepardec.mega.domain.model.SourceSystem;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.UserService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.FromStringTerm;
import jakarta.mail.search.SearchTerm;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Dependent
public class EmailReceiver {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final String NACHRICHT = "Nachricht";
    private static final String ZEP_ID = "User-ID";
    private static final String MITARBEITER = "Mitarbeiter";
    private static final String PROJEKT = "Projekt";
    private static final String VORGANG = "Vorgang";
    private static final String BEMERKUNGEN = "Bemerkungen";

    @Inject
    Logger logger;

    @Inject
    MailReceiverConfig mailReceiverConfig;

    @Inject
    UserService userService;

    @Inject
    CommentService commentService;

    public void retrieveEmailsAndSaveToComments() {
        var properties = createMailProperties();

        try {
            var session = Session.getDefaultInstance(properties);
            var store = session.getStore(mailReceiverConfig.getProtocol());
            store.connect(
                    mailReceiverConfig.getHost(),
                    mailReceiverConfig.getUsername(),
                    mailReceiverConfig.getPassword()
            );

            var inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_WRITE);
            logger.info("Inbox opened.");

            var messages = inbox.search(bySenderAndUnseen());
            logger.info("Found {} relevant messages.", messages.length);

            for (Message message : messages) {
                var multipartContent = (Multipart) message.getContent();
                var content = multipartContent.getBodyPart(0).getContent().toString().strip();
                var contentMap = Stream.of(content.split("\n"))
                        .map(String::strip)
                        .map(line -> toMapEntry(line.split(":")))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                var zepNotificationMail = ZepNotificationMail.builder()
                        .withTag(parseDate(message.getSubject()))
                        .withNachricht(contentMap.get(NACHRICHT))
                        .withZepId(contentMap.get(ZEP_ID))
                        .withBuchungInfo(content.split("\n")[0].strip())
                        .withMitarbeiterVorname(extractVorname(contentMap.get(MITARBEITER)))
                        .withMitarbeiterNachname(extractNachname(contentMap.get(MITARBEITER)))
                        .withProjekt(contentMap.get(PROJEKT))
                        .withVorgang(contentMap.get(VORGANG))
                        .withBemerkung(contentMap.get(BEMERKUNGEN))
                        .build();

                var user = userService.findByName(
                        zepNotificationMail.getMitarbeiterVorname(),
                        zepNotificationMail.getMitarbeiterNachname()
                );

                var ersteller = userService.findByZepId(zepNotificationMail.getZepIdErsteller());

                commentService.create(
                        StepName.CONTROL_TIME_EVIDENCES.getId(),
                        SourceSystem.ZEP,
                        user.getEmail(),
                        buildComment(zepNotificationMail),
                        ersteller.getEmail(),
                        zepNotificationMail.getProjekt(),
                        zepNotificationMail.getTag().toString()
                );

                message.setFlag(Flags.Flag.SEEN, true);
            }

            inbox.close(false);
            store.close();
            logger.info("Inbox closed.");
        } catch (Exception e) {
            logger.error("Error retrieving E-Mails from Mailbox: ", e);
        }
    }

    private Properties createMailProperties() {
        var properties = new Properties();
        properties.put("mail.store.protocol", mailReceiverConfig.getProtocol());
        properties.put("mail.imaps.host", mailReceiverConfig.getHost());
        properties.put("mail.imaps.port", mailReceiverConfig.getPort());

        return properties;
    }

    private SearchTerm bySenderAndUnseen() {
        var senderTerm = new FromStringTerm(mailReceiverConfig.getSender());

        var seen = new Flags(Flags.Flag.SEEN);
        var unseenFlagTerm = new FlagTerm(seen, false);

        return new AndTerm(unseenFlagTerm, senderTerm);
    }

    private static String buildComment(ZepNotificationMail mail) {
        return String.format(
                "Deine Buchung vom %s im Projekt '%s' weist beim Vorgang '%s' %s " +
                        "mit der Bemerkung '%s' einen Fehler auf. %s",
                DATE_FORMATTER.format(mail.getTag()),
                mail.getProjekt(),
                mail.getVorgang(),
                mail.getBuchungInfo(),
                mail.getBemerkung(),
                mail.getNachricht()
        );
    }

    private static Map.Entry<String, String> toMapEntry(String[] keyValue) {
        if (keyValue.length != 2) {
            return null;
        }

        return Map.entry(keyValue[0].strip(), keyValue[1].strip());
    }

    private static LocalDate parseDate(String subject) {
        var regex = "\\d{2}\\.\\d{2}\\.\\d{4}";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(subject);

        if (matcher.find()) {
            return LocalDate.parse(matcher.group(), DATE_FORMATTER);
        }

        return null;
    }

    private static String extractNachname(String name) {
        return name != null ? name.split(",")[0].strip() : null;
    }

    private static String extractVorname(String name) {
        return name != null ? name.split(",")[1].strip() : null;
    }
}

