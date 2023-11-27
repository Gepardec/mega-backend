package com.gepardec.mega.notification.mail.receiver;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class ZepProjektzeitDetailsMailMapper implements ZepMailMapper<ZepProjektzeitDetailsMail> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String SUBJECT_REGEX = "Projektzeit (.*)";

    private static final String NACHRICHT = "Nachricht";
    private static final String ZEP_ID_ERSTELLER = "Ersteller-ID";
    private static final String MITARBEITER = "Mitarbeiter";
    private static final String PROJEKT = "Projekt";
    private static final String VORGANG = "Vorgang";
    private static final String BEMERKUNGEN = "Bemerkungen";

    @Inject
    Logger logger;

    @Override
    public Optional<ZepProjektzeitDetailsMail> convert(Message message) throws MessagingException, IOException {
        var subject = message.getSubject();
        var multipartContent = (Multipart) message.getContent();
        var content = multipartContent.getBodyPart(0).getContent().toString().strip();
        var contentMap = Stream.of(content.split("\n"))
                .map(String::strip)
                .map(line -> toMapEntry(line.split(":")))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (isSubjectValid(subject) && isBodyValid(contentMap)) {
            return Optional.of(
                    ZepProjektzeitDetailsMail.builder()
                            .withTag(parseDate(subject))
                            .withNachricht(contentMap.get(NACHRICHT))
                            .withZepIdErsteller(contentMap.get(ZEP_ID_ERSTELLER))
                            .withBuchungInfo(content.split("\n")[0].strip())
                            .withMitarbeiterVorname(extractVorname(contentMap.get(MITARBEITER)))
                            .withMitarbeiterNachname(extractNachname(contentMap.get(MITARBEITER)))
                            .withProjekt(contentMap.get(PROJEKT))
                            .withVorgang(contentMap.get(VORGANG))
                            .withBemerkung(contentMap.get(BEMERKUNGEN))
                            .build()
            );
        } else {
            logger.error("Subject or body of E-Mail is not parseable.");
            return Optional.empty();
        }
    }

    private static Map.Entry<String, String> toMapEntry(String[] keyValue) {
        if (keyValue.length != 2) {
            return null;
        }

        return Map.entry(keyValue[0].strip(), keyValue[1].strip());
    }

    private boolean isSubjectValid(String subject) {
        var pattern = Pattern.compile(SUBJECT_REGEX);
        var matcher = pattern.matcher(subject);

        return matcher.find();
    }

    private boolean isBodyValid(Map<String, String> contentMap) {
        return contentMap.keySet().containsAll(
                List.of(
                        NACHRICHT,
                        ZEP_ID_ERSTELLER,
                        MITARBEITER,
                        PROJEKT,
                        VORGANG,
                        BEMERKUNGEN
                )
        );
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
