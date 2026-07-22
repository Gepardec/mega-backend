package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.application.port.inbound.GetLeadProjectsUseCase;
import com.gepardec.mega.hexagon.project.application.port.inbound.SetLeistungsnachweisEnabledUseCase;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.shared.application.security.ForbiddenException;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Set;

@ApplicationScoped
@Transactional
public class ProjectSettingsService implements GetLeadProjectsUseCase, SetLeistungsnachweisEnabledUseCase {
    private final ProjectRepository projectRepository;

    @Inject
    public ProjectSettingsService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<Project> getLeadProjects(UserId actorId) {
        return projectRepository.findAllByLead(actorId);
    }

    @Override
    public void setLeistungsnachweisEnabled(ProjectId projectId, UserId actorId, boolean enabled) {
        List<Project> projects = projectRepository.findAllByIds(Set.of(projectId));
        if(projects.isEmpty()) throw new IllegalArgumentException("Project not found: " + projectId);

        Project project = projects.getFirst();
        if(!project.leads().contains(actorId)) throw new ForbiddenException("Actor is not a lead " + actorId);

        Project updated = project.withLeistungsnachweisEnabled(enabled);
        projectRepository.saveAll(List.of(updated));
    }
}
