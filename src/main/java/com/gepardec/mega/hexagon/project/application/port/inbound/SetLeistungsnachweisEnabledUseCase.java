package com.gepardec.mega.hexagon.project.application.port.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

public interface SetLeistungsnachweisEnabledUseCase {
    void setLeistungsnachweisEnabled(ProjectId projectId, UserId actorId, boolean enabled);
}
