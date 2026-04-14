package com.gepardec.mega.hexagon.shared.domain.model;

import java.util.Objects;

public record ProjectRef(
        ProjectId id,
        int zepId,
        String name
) {

    public ProjectRef {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}
