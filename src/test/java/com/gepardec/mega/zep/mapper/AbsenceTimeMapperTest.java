package com.gepardec.mega.zep.mapper;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.monthlyreport.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.provantis.zep.ProjektzeitType;
import de.provantis.zep.FehlzeitType;
import de.provantis.zep.AttributesType;
import de.provantis.zep.AttributeType;

public class AbsenceTimeMapperTest {

    @Test
    void whenNull_thenReturnsNull() {
        assertThat(AbsenceTimeMapper.map(null)).isNull();
    }

    @Test
    void whenAttributes_thenReturnsAttributesMap() {
        FehlzeitType fzt = new FehlzeitType();
        fzt.setUserId("2");
        fzt.setStartdatum("2023-10-23");
        fzt.setFehlgrund("KR");

        AttributeType attribute = new AttributeType();
        attribute.setName("gilde");
        attribute.setValue("cia");
        AttributeType attribute2 = new AttributeType();
        attribute2.setName("office");
        attribute2.setValue("wien");

        List<AttributeType> attributesList = new ArrayList<>();
        attributesList.add(attribute);
        attributesList.add(attribute2);

        AttributesType attributes = new AttributesType();
        attributes.setAttribute(attributesList);
        fzt.setAttributes(attributes);

        AbsenceTime at = AbsenceTimeMapper.map(fzt);
        Map<String, String> atAttributesMap = at.getAttributes();

        assertThat(atAttributesMap.get(attribute.getName())).isEqualTo(attribute.getValue());
        assertThat(atAttributesMap.get(attribute2.getName())).isEqualTo(attribute2.getValue());

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

        String absenceTimeStartDateString = at.getFromDate().toString();
        String absenceTimeEndDateString = at.getToDate().toString();

        assertThat(at.getId()).isEqualTo(fzt.getId());
        assertThat(at.getUserId()).isEqualTo(fzt.getUserId());
        assertThat(absenceTimeStartDateString).isEqualTo(fzt.getStartdatum());
        assertThat(absenceTimeEndDateString).isEqualTo(fzt.getEnddatum());
        assertThat(at.getReason()).isEqualTo(fzt.getFehlgrund());
        assertThat(at.getIsHalfADay()).isEqualTo(fzt.isIstHalberTag());
        assertThat(at.getAccepted()).isEqualTo(fzt.isGenehmigt());
        assertThat(at.getComment()).isEqualTo(fzt.getBemerkung());
        assertThat(at.getTimezone()).isEqualTo(fzt.getTimezone());
        assertThat(at.getSuppressMails()).isEqualTo(fzt.isMailversandUnterdruecken());
        assertThat(at.getCreated()).isEqualTo(fzt.getCreated());
        assertThat(at.getModified()).isEqualTo(fzt.getModified());
        assertThat(at.getReason()).isEqualTo(fzt.getFehlgrund());

        assertThat(at.getId()).isEqualTo(fzt.getId());
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

        assertThat(atList.get(0).getUserId()).isEqualTo(fztList.get(0).getUserId());
        assertThat(atList.get(1).getUserId()).isEqualTo(fztList.get(1).getUserId());
    }

    @Test
    void whenNullInList_thenFiltersNullElement() {
        assertThat(AbsenceTimeMapper.mapList(Collections.singletonList(null))).isEmpty();
    }


}
