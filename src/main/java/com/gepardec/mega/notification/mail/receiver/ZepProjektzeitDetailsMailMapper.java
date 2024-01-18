package com.gepardec.mega.notification.mail.receiver;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import org.apache.commons.lang3.StringUtils;
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

@ApplicationScoped
public class ZepProjektzeitDetailsMailMapper implements ZepMailMapper<ZepProjektzeitDetailsMail> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String SUBJECT_REGEX = "Projektzeit (.*) von (.*) bis (.*) \\((.*)\\)";
    private static final String PROJECT_NAME_REGEX = "(\\w*) \\(.*\\)";

    private static final String HINT_LINE = "Hinweis:";
    private static final String METADATA_MARKER = "#METADATEN####################################";
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
        var content = multipartContent.getBodyPart(0).getContent().toString();
        var nachricht = content.lines()
                .takeWhile(line -> !line.startsWith(METADATA_MARKER))
                .filter(line -> !line.startsWith(HINT_LINE))
                .filter(StringUtils::isNotBlank)
                .reduce("", (result, line) -> result.concat(" ").concat(line))
                .strip();

        var metadataMap = content.lines()
                .dropWhile(line -> !line.startsWith(METADATA_MARKER))
                .skip(1) // skip the line with METADATA_MARKER
                .filter(StringUtils::isNotBlank)
                .map(line -> toMapEntry(line.split(":")))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (isSubjectValid(subject) && isBodyValid(metadataMap)) {
            return Optional.of(
                    ZepProjektzeitDetailsMail.builder()
                            .withTag(parseDate(subject))
                            .withUhrzeitVon(parseUhrzeitVon(subject))
                            .withUhrzeitBis(parseUhrzeitBis(subject))
                            .withNachricht(nachricht)
                            .withZepIdErsteller(metadataMap.get(ZEP_ID_ERSTELLER))
                            .withMitarbeiterVorname(extractVorname(metadataMap.get(MITARBEITER)))
                            .withMitarbeiterNachname(extractNachname(metadataMap.get(MITARBEITER)))
                            .withProjekt(extractProjekt(metadataMap.get(PROJEKT)))
                            .withVorgang(metadataMap.get(VORGANG))
                            .withBemerkung(metadataMap.get(BEMERKUNGEN))
                            .withRawContent("Subject: " + subject + "\n" + "Body: " + content)
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

    private static String parseUhrzeitVon(String subject) {
        var pattern = Pattern.compile(SUBJECT_REGEX);
        var matcher = pattern.matcher(subject);

        if (matcher.find()) {
            return matcher.group(2);
        }

        return null;
    }

    private static String parseUhrzeitBis(String subject) {
        var pattern = Pattern.compile(SUBJECT_REGEX);
        var matcher = pattern.matcher(subject);

        if (matcher.find()) {
            return matcher.group(3);
        }

        return null;
    }

    private static String extractProjekt(String projekt) {
        var pattern = Pattern.compile(PROJECT_NAME_REGEX);
        var matcher = pattern.matcher(projekt);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return projekt;
    }
}
