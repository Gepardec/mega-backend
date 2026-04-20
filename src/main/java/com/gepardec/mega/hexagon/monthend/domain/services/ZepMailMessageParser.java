package com.gepardec.mega.hexagon.monthend.domain.services;

import com.gepardec.mega.hexagon.monthend.domain.model.ZepMailParseResult;
import com.gepardec.mega.hexagon.monthend.domain.model.ZepProjektzeitEntry;
import com.gepardec.mega.hexagon.monthend.domain.model.ZepRawMail;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
public class ZepMailMessageParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String SUBJECT_REGEX = "Projektzeit (.*) von (.*) bis (.*) \\((.*)\\)";
    private static final String PROJECT_NAME_REGEX = "^(.*?)\\s*\\(";
    private static final String ZEP_ID_ERSTELLER = "Ersteller-ID";
    private static final String MITARBEITER = "Mitarbeiter";
    private static final String PROJEKT = "Projekt";
    private static final String VORGANG = "Vorgang";
    private static final String BEMERKUNGEN = "Bemerkungen";
    private static final String ANMERKUNG = "Anmerkung";
    private static final Pattern TABLE_ROW_PATTERN = Pattern.compile(
            "<tr>\\s*<td>(.*?)</td>\\s*<td>.*?<span>(.*?)</span>.*?</td>\\s*</tr>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    public ZepMailParseResult parse(ZepRawMail rawMail) {
        Map<String, String> tableMap = parseTableToMap(rawMail.htmlBody());

        Optional<ZepUsername> creatorUsername = Optional.ofNullable(tableMap.get(ZEP_ID_ERSTELLER))
                .filter(value -> !value.isBlank())
                .map(ZepUsername::of);

        if (!isSubjectValid(rawMail.subject()) || !isBodyValid(tableMap)) {
            return new ZepMailParseResult(Optional.empty(), creatorUsername);
        }

        String tableHtml = extractTableHtml(rawMail.htmlBody());
        ZepProjektzeitEntry entry = new ZepProjektzeitEntry(
                parseDate(rawMail.subject()),
                parseTimeFrom(rawMail.subject()),
                parseTimeTo(rawMail.subject()),
                tableHtml,
                creatorUsername.orElse(null),
                extractFirstName(tableMap.get(MITARBEITER)),
                extractLastName(tableMap.get(MITARBEITER)),
                extractProjectName(tableMap.get(PROJEKT)),
                tableMap.get(VORGANG),
                tableMap.get(BEMERKUNGEN),
                tableMap.get(ANMERKUNG)
        );

        return new ZepMailParseResult(Optional.of(entry), creatorUsername);
    }

    private Map<String, String> parseTableToMap(String html) {
        Map<String, String> result = new LinkedHashMap<>();
        var matcher = TABLE_ROW_PATTERN.matcher(html);
        while (matcher.find()) {
            result.put(matcher.group(1).strip(), matcher.group(2).strip());
        }
        return result;
    }

    private String extractTableHtml(String html) {
        int start = html.indexOf("<table");
        int end = html.lastIndexOf("</table>") + "</table>".length();
        if (start >= 0 && end > start) {
            return html.substring(start, end);
        }
        return html;
    }

    private boolean isSubjectValid(String subject) {
        return Pattern.compile(SUBJECT_REGEX).matcher(subject).find();
    }

    private boolean isBodyValid(Map<String, String> tableMap) {
        return tableMap.keySet().containsAll(List.of(ZEP_ID_ERSTELLER, MITARBEITER, PROJEKT, VORGANG, BEMERKUNGEN, ANMERKUNG))
                && !tableMap.get(ANMERKUNG).isBlank();
    }

    private LocalDate parseDate(String subject) {
        var matcher = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}").matcher(subject);
        return matcher.find() ? LocalDate.parse(matcher.group(), DATE_FORMATTER) : null;
    }

    private String parseTimeFrom(String subject) {
        var matcher = Pattern.compile(SUBJECT_REGEX).matcher(subject);
        return matcher.find() ? matcher.group(2) : null;
    }

    private String parseTimeTo(String subject) {
        var matcher = Pattern.compile(SUBJECT_REGEX).matcher(subject);
        return matcher.find() ? matcher.group(3) : null;
    }

    private String extractFirstName(String employeeName) {
        return employeeName != null ? employeeName.split(",", 2)[1].strip() : null;
    }

    private String extractLastName(String employeeName) {
        return employeeName != null ? employeeName.split(",", 2)[0].strip() : null;
    }

    private String extractProjectName(String project) {
        var matcher = Pattern.compile(PROJECT_NAME_REGEX).matcher(project);
        return matcher.find() ? matcher.group(1) : project;
    }
}
