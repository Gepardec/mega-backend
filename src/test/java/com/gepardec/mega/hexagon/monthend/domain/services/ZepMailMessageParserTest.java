package com.gepardec.mega.hexagon.monthend.domain.services;

import com.gepardec.mega.hexagon.monthend.domain.model.ZepRawMail;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ZepMailMessageParserTest {

    private static final String SUBJECT = "Projektzeit Fr, 03.11.2023 von 11:15 bis 12:00 (0,75 Stunden)";
    private static final String HTML_BODY = "<table><tbody>" +
            "<tr><td>Ersteller-ID</td><td><div>001-mmustermann</div></td></tr>" +
            "<tr><td>Mitarbeiter</td><td><div>Mustermann, Max</div></td></tr>" +
            "<tr><td>Projekt</td><td><div>Gepardec SE-Gilde (Ein cooles Projekt)</div></td></tr>" +
            "<tr><td>Vorgang</td><td><div>Learning Friday</div></td></tr>" +
            "<tr><td>Tätigkeit</td><td><div>bearbeiten</div></td></tr>" +
            "<tr><td>Bemerkung</td><td><div>MEGA</div></td></tr>" +
            "<tr><td><b>Anmerkung</b></td><td><b>Projekt passt nicht, bitte anpassen!</b></td></tr>" +
            "</tbody></table>";

    private final ZepMailMessageParser parser = new ZepMailMessageParser();

    @Test
    void parse_shouldReturnParsedZepProjektzeitEntryForValidMail() {
        var result = parser.parse(new ZepRawMail(SUBJECT, HTML_BODY));

        assertThat(result.entry()).isPresent();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.entry().get().date()).isEqualTo(LocalDate.of(2023, 11, 3));
            softly.assertThat(result.entry().get().timeFrom()).isEqualTo("11:15");
            softly.assertThat(result.entry().get().timeTo()).isEqualTo("12:00");
            softly.assertThat(result.entry().get().message()).isEqualTo(HTML_BODY);
            softly.assertThat(result.entry().get().employeeFirstName()).isEqualTo("Max");
            softly.assertThat(result.entry().get().employeeLastName()).isEqualTo("Mustermann");
            softly.assertThat(result.entry().get().projectName()).isEqualTo("Gepardec SE-Gilde");
            softly.assertThat(result.entry().get().task()).isEqualTo("Learning Friday");
            softly.assertThat(result.entry().get().remark()).isEqualTo("MEGA");
            softly.assertThat(result.entry().get().clarification()).isEqualTo("Projekt passt nicht, bitte anpassen!");
        });
    }

    @Test
    void parse_shouldAlwaysExtractCreatorEvenWhenEntryIsInvalid() {
        var result = parser.parse(new ZepRawMail("Invalid subject", HTML_BODY));

        assertThat(result.entry()).isEmpty();
        assertThat(result.creatorUsername()).contains(ZepUsername.of("001-mmustermann"));
    }

    @Test
    void parse_shouldReturnEmptyEntryForInvalidSubject() {
        assertThat(parser.parse(new ZepRawMail("Invalid subject", HTML_BODY)).entry()).isEmpty();
    }

    @Test
    void parse_shouldReturnEmptyEntryWhenRequiredFieldIsMissing() {
        String bodyMissingAnmerkung = "<table><tbody>" +
                "<tr><td>Ersteller-ID</td><td><div><span>001-mmustermann</span></div></td></tr>" +
                "<tr><td>Mitarbeiter</td><td><div><span>Mustermann, Max</span></div></td></tr>" +
                "<tr><td>Projekt</td><td><div><span>Gepardec</span></div></td></tr>" +
                "<tr><td>Vorgang</td><td><div><span>Learning Friday</span></div></td></tr>" +
                "<tr><td>Bemerkungen</td><td><div><span>MEGA</span></div></td></tr>" +
                "</tbody></table>";

        assertThat(parser.parse(new ZepRawMail(SUBJECT, bodyMissingAnmerkung)).entry()).isEmpty();
    }

    @Test
    void parse_shouldReturnEmptyEntryWhenAnmerkungIsBlank() {
        String bodyWithBlankAnmerkung = "<table><tbody>" +
                "<tr><td>Ersteller-ID</td><td><div><span>001-mmustermann</span></div></td></tr>" +
                "<tr><td>Mitarbeiter</td><td><div><span>Mustermann, Max</span></div></td></tr>" +
                "<tr><td>Projekt</td><td><div><span>Gepardec</span></div></td></tr>" +
                "<tr><td>Vorgang</td><td><div><span>Learning Friday</span></div></td></tr>" +
                "<tr><td>Bemerkungen</td><td><div><span>MEGA</span></div></td></tr>" +
                "<tr><td>Anmerkung</td><td><div><span>   </span></div></td></tr>" +
                "</tbody></table>";

        assertThat(parser.parse(new ZepRawMail(SUBJECT, bodyWithBlankAnmerkung)).entry()).isEmpty();
    }
}
