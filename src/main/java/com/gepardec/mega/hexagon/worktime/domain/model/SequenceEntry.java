package com.gepardec.mega.hexagon.worktime.domain.model;

import java.util.Objects;
import java.util.Optional;

public record SequenceEntry<T>(T current, Optional<T> next) {
    public SequenceEntry {
        Objects.requireNonNull(current, "current must not be null");
        Objects.requireNonNull(next, "next must not be null");
    }
}
