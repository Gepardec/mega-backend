package com.gepardec.mega.hexagon.worktime.domain.model;

import java.util.Arrays;
import java.util.Optional;

public enum JourneyDirection {
    TO("0"),
    FURTHER("1"),
    BACK("2");

    private final String direction;

    JourneyDirection(String direction) {
        this.direction = direction;
    }

    public static Optional<JourneyDirection> fromString(String direction) {
        String normalized = direction == null || direction.isBlank() ? TO.direction : direction;
        return Arrays.stream(values()).filter(value -> value.direction.equalsIgnoreCase(normalized)).findFirst();
    }

    public String getDirection() {
        return direction;
    }
}
