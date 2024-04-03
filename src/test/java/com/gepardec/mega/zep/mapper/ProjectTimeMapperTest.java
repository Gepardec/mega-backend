package com.gepardec.mega.zep.mapper;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.ProjectTime;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.provantis.zep.ProjektzeitType;
import de.provantis.zep.AttributesType;
import de.provantis.zep.AttributeType;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectTimeMapperTest {



    @Test
    void whenNull_thenReturnsNull() {
        assertThat(ProjectTimeMapper.map(null)).isNull();
    }


    @Test
    void withFullSettings_thenReturnsAbsenceTimeObject() {
        ProjektzeitType pzt = new ProjektzeitType();
        pzt.setId("1");
        pzt.setUserId("1");
        pzt.setDatum("2024-01-15");
        pzt.setVon("12:31:22");
        pzt.setBis("15:12:57");
        pzt.setDauer("3");
        pzt.setIstFakturierbar(true);
        pzt.setIstOrtProjektRelevant(true);
        pzt.setOrt("Schusslinie 1, 1110 Wien");
        pzt.setBemerkung("Ort Meldeadresse");
        pzt.setStart("Ernst-Melchior-Gasse 20, 1020 Wien");
        pzt.setZiel("Schusslinie 1, 1110 Wien");
        pzt.setKm(7);
        pzt.setAnzahlMitfahrer(1);
        pzt.setFahrzeug("Elektrotriebwagen 4020");
        pzt.setTicketNr(1);
        pzt.setTeilaufgabeNr("feature/727");
        pzt.setReiseRichtung("Wolfsdorf");
        pzt.setIstPrivatFahrzeug(false);
        pzt.setCreated("2024-01-15");
        pzt.setModified("2024-01-16");

        ProjectTime pt = ProjectTimeMapper.map(pzt);

        assertThat(pt.getUserId()).isEqualTo(pzt.getUserId());
        assertThat(pt.getDuration()).isEqualTo(pzt.getDauer());
        assertThat(pt.getBillable()).isEqualTo(pzt.isIstFakturierbar());
    }

    @Test
    void whenEmptyList_thenReturnsEmptyList() {
        assertThat(ProjectTimeMapper.mapList(List.of())).isEmpty();
    }
    @Test
    void whenList_thenReturnList() {
        ProjektzeitType pzt = new ProjektzeitType();
        pzt.setUserId("1");

        ProjektzeitType pzt2 = new ProjektzeitType();
        pzt2.setUserId("1");

        ProjektzeitType[] fztArr = {pzt, pzt2};
        List<ProjektzeitType> pztList = List.of(fztArr);
        List<ProjectTime> ptList = ProjectTimeMapper.mapList(pztList);

        assertThat(ptList.get(0).getUserId()).isEqualTo(pztList.get(0).getUserId());
        assertThat(ptList.get(1).getUserId()).isEqualTo(pztList.get(1).getUserId());
    }

    @Test
    void whenNullInList_thenFiltersNullElement() {
        assertThat(ProjectTimeMapper.mapList(Collections.singletonList(null))).isEmpty();
    }


}

