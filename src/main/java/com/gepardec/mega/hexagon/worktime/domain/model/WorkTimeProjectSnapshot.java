package com.gepardec.mega.hexagon.worktime.domain.model;

import com.gepardec.mega.hexagon.project.domain.model.ProjectId;

import java.util.Objects;

public record WorkTimeProjectSnapshot(
        ProjectId id,
        int zepId,
        String name
) {

    public WorkTimeProjectSnapshot {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}
