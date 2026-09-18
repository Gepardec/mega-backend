package com.gepardec.mega.hexagon.project.application.port.inbound;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.util.List;

public interface GetProjectSettingsUseCase {
    List<Project> getLeadProjects(UserId actorId);
}
