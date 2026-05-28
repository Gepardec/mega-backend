package com.gepardec.mega.hexagon.monthend.domain.model;

import java.util.UUID;

public record MonthEndClarificationId(UUID value) {

    public static MonthEndClarificationId generate() {
        return new MonthEndClarificationId(UUID.randomUUID());
    }

    public static MonthEndClarificationId of(UUID value) {
        return new MonthEndClarificationId(value);
    }
}
