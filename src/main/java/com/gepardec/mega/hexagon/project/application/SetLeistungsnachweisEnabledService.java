package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.application.port.inbound.SetLeistungsnachweisEnabledUseCase;
import com.gepardec.mega.hexagon.project.domain.error.ProjectNotFoundException;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.shared.application.security.ForbiddenException;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
@Transactional
public class SetLeistungsnachweisEnabledService implements SetLeistungsnachweisEnabledUseCase {

    private final ProjectRepository projectRepository;

    @Inject
    public SetLeistungsnachweisEnabledService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public void setLeistungsnachweisEnabled(ProjectId projectId, UserId actorId, boolean enabled) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + projectId));

        if (!project.isLedBy(actorId)) {
            throw new ForbiddenException("Actor is not a lead " + actorId);
        }

        boolean previousValue = project.leistungsnachweisEnabled();
        Project updated = project.withLeistungsnachweisEnabled(enabled);
        projectRepository.saveAll(List.of(updated));

        Log.infof("Leistungsnachweis for project %s changed by %s: %s -> %s",
                projectId, actorId, previousValue, enabled);
    }
}
