package com.gepardec.mega.domain.model.monthlyreport;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


//TODO direction_of_travel can be null in the REP REST Response, for now we default to 0 if that is the case
// decide if we want to keep it that way
public enum JourneyDirection {
    TO("0"),
    FURTHER("1"),
    BACK("2");

    private static final Map<String, JourneyDirection> enumMap = Stream.of(JourneyDirection.values())
            .collect(Collectors.toMap(journeyDirection -> journeyDirection.direction, Function.identity()));

    private final String direction;

    JourneyDirection(String direction) {
        this.direction = direction;
    }

    public static Optional<JourneyDirection> fromString(String direction) {
        return Optional.ofNullable(enumMap.get(StringUtils.defaultIfEmpty(direction, TO.getDirection()).toUpperCase()));
    }

    public String getDirection() {
        return direction;
    }
}

