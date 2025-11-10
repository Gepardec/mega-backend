package com.gepardec.mega.service.mapper;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.utils.DateUtils;
import de.provantis.zep.BeschaeftigungszeitListeType;
import de.provantis.zep.BeschaeftigungszeitType;
import de.provantis.zep.MitarbeiterType;
import de.provantis.zep.RegelarbeitszeitListeTypeTs;
import de.provantis.zep.RegelarbeitszeitType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class EmployeeMapperTest {

    @Inject
    EmployeeMapper mapper;

    @Test
    void map_whenEmployeeIsNull_thenReturnsNull() {
        assertThat(mapper.map(null)).isNull();
    }

    @Test
    void map_whenEmployee_thenMappedProperly() {
        final BeschaeftigungszeitType activeEmployment = createBeschaeftigungszeitType(
                LocalDate.now().minusDays(7),
                LocalDate.now().plusDays(1)
        );
        final RegelarbeitszeitType regelarbeitszeit = createRegelarbeitszeitType(LocalDate.now().minusDays(7));
        final BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(activeEmployment));
        final RegelarbeitszeitListeTypeTs regelarbeitszeiten = createRegelarbeitszeitListeTypeTs(List.of(regelarbeitszeit));
        final MitarbeiterType employee = new MitarbeiterType();
        employee.setEmail("no-reply@gepardec.com");
        employee.setVorname("Max");
        employee.setNachname("Mustermann");
        employee.setTitel("Ing.");
        employee.setUserId("1");
        employee.setAnrede("Herr");
        employee.setPreisgruppe("ARCHITEKT");
        employee.setFreigabedatum("2020-01-01");
        employee.setBeschaeftigungszeitListe(employments);
        employee.setRegelarbeitszeitListe(regelarbeitszeiten);

        final Employee actual = mapper.map(employee);
        assertThat(employee).isNotNull();
        assertThat(actual.getUserId()).isEqualTo(employee.getUserId());
        assertThat(actual.getEmail()).isEqualTo(employee.getEmail());
        assertThat(actual.getFirstname()).isEqualTo(employee.getVorname());
        assertThat(actual.getLastname()).isEqualTo(employee.getNachname());
        assertThat(actual.getTitle()).isEqualTo(employee.getTitel());
        assertThat(actual.getSalutation()).isEqualTo(employee.getAnrede());
        assertThat(actual.getWorkDescription()).isEqualTo(employee.getPreisgruppe());
        assertThat(actual.getReleaseDate()).isEqualTo(employee.getFreigabedatum());
        assertThat(actual.getEmploymentPeriods()).isNotNull();
        assertThat(actual.getRegularWorkingTimes()).isNotNull();
    }

    private BeschaeftigungszeitType createBeschaeftigungszeitType(final LocalDate start, final LocalDate end) {
        final BeschaeftigungszeitType beschaeftigung = new BeschaeftigungszeitType();
        beschaeftigung.setStartdatum((start != null) ? DateUtils.formatDate(start) : null);
        beschaeftigung.setEnddatum((end != null) ? DateUtils.formatDate(end) : null);
        return beschaeftigung;
    }

    private BeschaeftigungszeitListeType createBeschaeftigungszeitListeType(List<BeschaeftigungszeitType> employments) {
        final BeschaeftigungszeitListeType beschaeftigungszeitListeType = new BeschaeftigungszeitListeType();
        beschaeftigungszeitListeType.getBeschaeftigungszeit().addAll(employments);
        return beschaeftigungszeitListeType;
    }

    private RegelarbeitszeitType createRegelarbeitszeitType(final LocalDate start) {
        final RegelarbeitszeitType regelarbeitszeit = new RegelarbeitszeitType();
        regelarbeitszeit.setStartdatum(start != null ? DateUtils.formatDate(start) : null);
        regelarbeitszeit.setMontag(8.0d);
        regelarbeitszeit.setDienstag(8.0d);
        regelarbeitszeit.setMittwoch(8.0d);
        regelarbeitszeit.setDonnerstag(8.0d);
        regelarbeitszeit.setFreitag(6.5d);
        regelarbeitszeit.setSamstag(0d);
        regelarbeitszeit.setSonntag(0d);
        return regelarbeitszeit;
    }

    private RegelarbeitszeitListeTypeTs createRegelarbeitszeitListeTypeTs(List<RegelarbeitszeitType> regelarbeitszeiten) {
        final RegelarbeitszeitListeTypeTs regelarbeitszeitListeTypeTs = new RegelarbeitszeitListeTypeTs();
        regelarbeitszeitListeTypeTs.getRegelarbeitszeit().addAll(regelarbeitszeiten);
        return regelarbeitszeitListeTypeTs;
    }
}
