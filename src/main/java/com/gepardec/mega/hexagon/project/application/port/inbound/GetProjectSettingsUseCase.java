package com.gepardec.mega.hexagon.project.application.port.inbound;

import com.gepardec.mega.hexagon.project.domain.model.Project;

import java.util.List;

public interface GetProjectSettingsUseCase {
    List<Project> getSettings();
}
