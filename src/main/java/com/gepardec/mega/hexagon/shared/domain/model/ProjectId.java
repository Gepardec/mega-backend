package com.gepardec.mega.hexagon.shared.domain.model;

import java.util.UUID;

public record ProjectId(UUID value) {

    public static ProjectId generate() {
        return new ProjectId(UUID.randomUUID());
    }

    public static ProjectId of(UUID value) {
        return new ProjectId(value);
    }
}
