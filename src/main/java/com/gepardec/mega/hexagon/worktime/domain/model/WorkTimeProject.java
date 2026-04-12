package com.gepardec.mega.hexagon.worktime.domain.model;

import com.gepardec.mega.hexagon.project.domain.model.ProjectId;

import java.util.Objects;

public record WorkTimeProject(
        ProjectId id,
        String name
) {

    public WorkTimeProject {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}
