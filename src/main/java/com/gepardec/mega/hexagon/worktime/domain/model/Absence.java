package com.gepardec.mega.hexagon.worktime.domain.model;

import java.time.LocalDate;
import java.util.Objects;

public record Absence(LocalDate date, AbsenceType type) {

    public Absence {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(type, "type must not be null");
    }
}
