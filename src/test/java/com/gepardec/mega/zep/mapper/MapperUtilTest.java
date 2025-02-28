package com.gepardec.mega.zep.mapper;

import de.provantis.zep.AttributeType;
import de.provantis.zep.AttributesType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MapperUtilTest {
    @Test
    void DateString_convertsToLocalDate() {
        String dateString = "2002-11-23";
        LocalDate actualDate = LocalDate.of(2002, 11, 23);
        LocalDate date = MapperUtil.convertStringToDate(dateString);
        assertThat(date).isEqualTo(actualDate);
    }

    @Test
    void whenAttributes_thenReturnsAttributesMap() {
        AttributeType attribute = new de.provantis.zep.AttributeType();
        attribute.setName("gilde");
        attribute.setValue("cia");
        AttributeType attribute2 = new de.provantis.zep.AttributeType();
        attribute2.setName("office");
        attribute2.setValue("wien");

        List<AttributeType> attributesList = new ArrayList<>();
        attributesList.add(attribute);
        attributesList.add(attribute2);

        AttributesType attributes = new de.provantis.zep.AttributesType();
        attributes.setAttribute(attributesList);

        Map<String, String> attributesMap = MapperUtil.convertAttributesToMap(attributes);

        assertThat(attributesMap.get(attribute.getName())).isEqualTo(attribute.getValue());
        assertThat(attributesMap.get(attribute2.getName())).isEqualTo(attribute2.getValue());

    }

    @Test
    void whenNoAttributes_thenReturnsEmptyMap() {
        assertThat(MapperUtil.convertAttributesToMap(null)).isEmpty();
    }
}
