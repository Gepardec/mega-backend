package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.application.port.inbound.ProjectLeadSyncResult;
import com.gepardec.mega.hexagon.project.application.port.inbound.SyncProjectLeadsUseCase;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.project.domain.port.outbound.UserIdentityLookupPort;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ZepProjectPort;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
@Transactional
public class SyncProjectLeadsService implements SyncProjectLeadsUseCase {

    private final ZepProjectPort zepProjectPort;
    private final ProjectRepository projectRepository;
    private final UserIdentityLookupPort userIdentityLookupPort;

    @Inject
    public SyncProjectLeadsService(ZepProjectPort zepProjectPort, ProjectRepository projectRepository,
                                   UserIdentityLookupPort userIdentityLookupPort) {
        this.zepProjectPort = zepProjectPort;
        this.projectRepository = projectRepository;
        this.userIdentityLookupPort = userIdentityLookupPort;
    }

    @Override
    public ProjectLeadSyncResult sync() {
        SyncAccumulator syncAccumulator = syncProjects(projectRepository.findAll());
        persistProjects(syncAccumulator.projectsToSave());

        return new ProjectLeadSyncResult(
                syncAccumulator.resolved(),
                syncAccumulator.skipped(),
                syncAccumulator.allLeadUserIds()
        );
    }

    private SyncAccumulator syncProjects(List<Project> projects) {
        SyncAccumulator syncAccumulator = new SyncAccumulator();
        for (Project project : projects) {
            syncAccumulator.recordProject(syncProject(project));
        }
        return syncAccumulator;
    }

    private ProjectLeadResolution syncProject(Project project) {
        Set<UserId> resolvedLeads = new HashSet<>();
        int resolved = 0;
        int skipped = 0;

        for (String username : zepProjectPort.fetchLeadUsernames(project.zepId())) {
            Optional<UserId> userId = userIdentityLookupPort.findUserIdByZepUsername(ZepUsername.of(username));
            if (userId.isPresent()) {
                resolvedLeads.add(userId.get());
                resolved++;
            } else {
                skipped++;
            }
        }

        Project updatedProject = project.withLeads(resolvedLeads);
        return new ProjectLeadResolution(
                updatedProject,
                resolvedLeads,
                resolved,
                skipped,
                !Objects.equals(project, updatedProject)
        );
    }

    private void persistProjects(List<Project> projectsToSave) {
        if (!projectsToSave.isEmpty()) {
            projectRepository.saveAll(projectsToSave);
        }
    }

    private record ProjectLeadResolution(
            Project project,
            Set<UserId> resolvedLeadIds,
            int resolved,
            int skipped,
            boolean changed
    ) {
    }

    private static final class SyncAccumulator {

        private final List<Project> projectsToSave = new java.util.ArrayList<>();
        private final Set<UserId> allLeadUserIds = new HashSet<>();
        private int resolved;
        private int skipped;

        void recordProject(ProjectLeadResolution projectLeadResolution) {
            allLeadUserIds.addAll(projectLeadResolution.resolvedLeadIds());
            resolved += projectLeadResolution.resolved();
            skipped += projectLeadResolution.skipped();

            if (projectLeadResolution.changed()) {
                projectsToSave.add(projectLeadResolution.project());
            }
        }

        List<Project> projectsToSave() {
            return projectsToSave;
        }

        Set<UserId> allLeadUserIds() {
            return allLeadUserIds;
        }

        int resolved() {
            return resolved;
        }

        int skipped() {
            return skipped;
        }
    }
}
