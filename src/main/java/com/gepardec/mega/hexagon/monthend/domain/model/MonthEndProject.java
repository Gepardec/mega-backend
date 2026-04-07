package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.project.domain.model.ProjectId;

import java.util.Objects;

public record MonthEndProject(
        ProjectId id,
        String name
) {

    public MonthEndProject {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}
