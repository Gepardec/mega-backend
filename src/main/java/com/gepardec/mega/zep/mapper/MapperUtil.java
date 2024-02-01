package com.gepardec.mega.zep.mapper;

import com.gepardec.mega.domain.utils.DateUtils;
import de.provantis.zep.AttributeType;
import de.provantis.zep.AttributesType;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class MapperUtil {
    public static Map<String, String> convertAttributesToMap(AttributesType attributes) {
        if (attributes == null)
            return null;
        return attributes.getAttribute().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(de.provantis.zep.AttributeType::getName, AttributeType::getValue));
    }

    public static LocalDate convertStringToDate(String dateString) {
        if (dateString == null)
            return null;

        return DateUtils.parseDate(dateString);

    }

}
