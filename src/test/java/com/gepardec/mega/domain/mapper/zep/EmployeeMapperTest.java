package com.gepardec.mega.domain.mapper.zep;

import com.gepardec.mega.domain.model.Employee;
import de.provantis.zep.BeschaeftigungszeitListeType;
import de.provantis.zep.RegelarbeitszeitListeTypeTs;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@QuarkusTest
public class EmployeeMapperTest {
    @Inject
    EmployeeMapper mapper;

    @Test
    public void mapToDomain_whenNull_thenReturnsNull() {
        // When
        var result = mapper.mapToDomain(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void mapToDomain_whenFullMitarbeiterType_thenReturnsFullEmployee() {
        // Given
        var mitarbeiterType = new de.provantis.zep.MitarbeiterType();
        mitarbeiterType.setUserId("userId");
        mitarbeiterType.setEmail("email");
        mitarbeiterType.setTitel("title");
        mitarbeiterType.setVorname("firstname");
        mitarbeiterType.setNachname("lastname");
        mitarbeiterType.setAnrede("salutation");
        mitarbeiterType.setFreigabedatum("releaseDate");
        mitarbeiterType.setPreisgruppe("workDescription");
        mitarbeiterType.setSprache("language");
        mitarbeiterType.setRegelarbeitszeitListe(new RegelarbeitszeitListeTypeTs());
        mitarbeiterType.setBeschaeftigungszeitListe(new BeschaeftigungszeitListeType());

        // When
        var result = mapper.mapToDomain(mitarbeiterType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("userId");
        assertThat(result.getEmail()).isEqualTo("email");
        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getFirstname()).isEqualTo("firstname");
        assertThat(result.getLastname()).isEqualTo("lastname");
        assertThat(result.getSalutation()).isEqualTo("salutation");
        assertThat(result.getReleaseDate()).isEqualTo("releaseDate");
        assertThat(result.getWorkDescription()).isEqualTo("workDescription");
        assertThat(result.getLanguage()).isEqualTo("language");
        assertThat(result.getRegularWorkingHours()).isNotNull();
        assertThat(result.isActive()).isFalse();
        assertThat(result.getExitDate()).isNull();
    }

}
