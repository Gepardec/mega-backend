package com.gepardec.mega.zep.mapper;

import com.gepardec.mega.domain.model.AbsenceTime;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import de.provantis.zep.FehlzeitType;

class AbsenceTimeMapperTest {

    @Test
    void whenNull_thenReturnsNull() {
        assertThat(AbsenceTimeMapper.map(null)).isNull();
    }


    @Test
    void withFullSettings_thenReturnsAbsenceTimeObject() {
        FehlzeitType fzt = new FehlzeitType();
        fzt.setId(1);
        fzt.setUserId("1");
        fzt.setStartdatum("2024-01-15");
        fzt.setEnddatum("2024-01-17");
        fzt.setFehlgrund("KA");
        fzt.setIstHalberTag(true);
        fzt.setGenehmigt(true);
        fzt.setBemerkung("Ort Meldeadresse");
        fzt.setTimezone("UTC");
        fzt.setMailversandUnterdruecken(true);
        fzt.setCreated("2024-01-15");
        fzt.setModified("2024-01-16");

        AbsenceTime at = AbsenceTimeMapper.map(fzt);

        String absenceTimeStartDateString = at.fromDate().toString();
        String absenceTimeEndDateString = at.toDate().toString();

        assertThat(at.userId()).isEqualTo(fzt.getUserId());
        assertThat(absenceTimeStartDateString).isEqualTo(fzt.getStartdatum());
        assertThat(absenceTimeEndDateString).isEqualTo(fzt.getEnddatum());
        assertThat(at.reason()).isEqualTo(fzt.getFehlgrund());
        assertThat(at.accepted()).isEqualTo(fzt.isGenehmigt());
    }

    @Test
    void whenEmptyList_thenReturnsEmptyList() {
        assertThat(AbsenceTimeMapper.mapList(List.of())).isEmpty();
    }
    @Test
    void whenList_thenReturnList() {
        FehlzeitType fzt = new FehlzeitType();
        fzt.setUserId("1");
        fzt.setStartdatum("2022-01-03");
        fzt.setFehlgrund("KA");
        fzt.setId(1);
        FehlzeitType fzt2 = new FehlzeitType();
        fzt2.setUserId("3");
        fzt2.setStartdatum("2024-01-03");
        fzt2.setFehlgrund("SW");
        fzt2.setId(2);
        FehlzeitType[] fztArr = {fzt, fzt2};
        List<FehlzeitType> fztList = List.of(fztArr);
        List<AbsenceTime> atList = AbsenceTimeMapper.mapList(fztList);

        assertThat(atList.get(0).userId()).isEqualTo(fztList.get(0).getUserId());
        assertThat(atList.get(1).userId()).isEqualTo(fztList.get(1).getUserId());
    }

    @Test
    void whenNullInList_thenFiltersNullElement() {
        assertThat(AbsenceTimeMapper.mapList(Collections.singletonList(null))).isEmpty();
    }


}
