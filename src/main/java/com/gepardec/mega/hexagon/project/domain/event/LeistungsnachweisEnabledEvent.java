package com.gepardec.mega.hexagon.project.domain.event;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;

import java.util.Objects;

public record LeistungsnachweisEnabledEvent(ProjectId projectId) {

    public LeistungsnachweisEnabledEvent {
        Objects.requireNonNull(projectId, "projectId must not be null");
    }
}
