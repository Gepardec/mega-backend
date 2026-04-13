package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;
import com.gepardec.mega.hexagon.project.domain.port.inbound.ProjectSyncResult;
import com.gepardec.mega.hexagon.project.domain.port.inbound.SyncProjectsUseCase;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ZepProjectPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class SyncProjectsService implements SyncProjectsUseCase {

    private final ZepProjectPort zepProjectPort;
    private final ProjectRepository projectRepository;

    @Inject
    public SyncProjectsService(ZepProjectPort zepProjectPort, ProjectRepository projectRepository) {
        this.zepProjectPort = zepProjectPort;
        this.projectRepository = projectRepository;
    }

    @Override
    public ProjectSyncResult sync() {
        List<ZepProjectProfile> zepProfiles = zepProjectPort.fetchAll();
        Map<Integer, Project> existingByZepId = findExistingProjects();
        SyncAccumulator syncAccumulator = synchronizeProjects(zepProfiles, existingByZepId);

        persistChanges(syncAccumulator.projectsToSave());

        return new ProjectSyncResult(syncAccumulator.created(), syncAccumulator.updated(), syncAccumulator.unchanged());
    }

    private Map<Integer, Project> findExistingProjects() {
        return projectRepository.findAll().stream()
                .collect(Collectors.toMap(Project::zepId, Function.identity()));
    }

    private SyncAccumulator synchronizeProjects(List<ZepProjectProfile> zepProfiles, Map<Integer, Project> existingByZepId) {
        SyncAccumulator syncAccumulator = new SyncAccumulator();
        for (ZepProjectProfile zepProject : zepProfiles) {
            syncAccumulator.recordProject(processProject(zepProject, existingByZepId.get(zepProject.zepId())));
        }
        return syncAccumulator;
    }

    private ProcessedProject processProject(ZepProjectProfile zepProject, Project existingProject) {
        Project synchronizedProject = existingProject != null
                ? existingProject.withSyncedZepData(zepProject)
                : Project.create(ProjectId.generate(), zepProject);

        return new ProcessedProject(synchronizedProject, determineChangeType(existingProject, synchronizedProject));
    }

    private ChangeType determineChangeType(Project existingProject, Project synchronizedProject) {
        if (existingProject == null) {
            return ChangeType.CREATED;
        }
        if (!Objects.equals(existingProject, synchronizedProject)) {
            return ChangeType.UPDATED;
        }
        return ChangeType.UNCHANGED;
    }

    private void persistChanges(List<Project> projectsToSave) {
        if (!projectsToSave.isEmpty()) {
            projectRepository.saveAll(projectsToSave);
        }
    }

    private record ProcessedProject(Project project, ChangeType changeType) {
    }

    private enum ChangeType {
        CREATED,
        UPDATED,
        UNCHANGED
    }

    private static final class SyncAccumulator {

        private final List<Project> projectsToSave = new ArrayList<>();
        private int created;
        private int updated;
        private int unchanged;

        void recordProject(ProcessedProject processedProject) {
            switch (processedProject.changeType()) {
                case CREATED -> {
                    created++;
                    projectsToSave.add(processedProject.project());
                }
                case UPDATED -> {
                    updated++;
                    projectsToSave.add(processedProject.project());
                }
                case UNCHANGED -> unchanged++;
            }
        }

        List<Project> projectsToSave() {
            return projectsToSave;
        }

        int created() {
            return created;
        }

        int updated() {
            return updated;
        }

        int unchanged() {
            return unchanged;
        }
    }
}
