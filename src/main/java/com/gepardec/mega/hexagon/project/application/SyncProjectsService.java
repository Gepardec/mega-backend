package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;
import com.gepardec.mega.hexagon.project.domain.port.inbound.ProjectSyncResult;
import com.gepardec.mega.hexagon.project.domain.port.inbound.SyncProjectsUseCase;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ZepProjectPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SyncProjectsService implements SyncProjectsUseCase {

    private final ZepProjectPort zepProjectPort;
    private final ProjectRepository projectRepository;

    public SyncProjectsService(ZepProjectPort zepProjectPort, ProjectRepository projectRepository) {
        this.zepProjectPort = zepProjectPort;
        this.projectRepository = projectRepository;
    }

    @Override
    public ProjectSyncResult sync() {
        List<ZepProjectProfile> zepProfiles = zepProjectPort.fetchAll();

        Map<Integer, Project> existingByZepId = projectRepository.findAll().stream()
                .collect(Collectors.toMap(Project::getZepId, Function.identity()));

        List<Project> toSave = new ArrayList<>();
        int created = 0;
        int updated = 0;

        for (ZepProjectProfile profile : zepProfiles) {
            Project project;
            Optional<Project> existing = Optional.ofNullable(existingByZepId.get(profile.zepId()));
            if (existing.isPresent()) {
                project = existing.get();
                project.syncFromZep(profile);
                updated++;
            } else {
                project = Project.create(ProjectId.generate(), profile);
                created++;
            }
            toSave.add(project);
        }

        projectRepository.saveAll(toSave);
        return new ProjectSyncResult(created, updated);
    }
}
