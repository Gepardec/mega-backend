package com.gepardec.mega.hexagon.monthend.domain.model;

import java.util.UUID;

public record MonthEndTaskId(UUID value) {

    public static MonthEndTaskId generate() {
        return new MonthEndTaskId(UUID.randomUUID());
    }

    public static MonthEndTaskId of(UUID value) {
        return new MonthEndTaskId(value);
    }
}
