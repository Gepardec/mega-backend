package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.port.inbound.ProjectLeadSyncResult;
import com.gepardec.mega.hexagon.project.domain.port.inbound.SyncProjectLeadsUseCase;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.project.domain.port.outbound.UserIdentityLookupPort;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ZepProjectPort;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
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
    private final UserRepository userRepository;

    @Inject
    public SyncProjectLeadsService(ZepProjectPort zepProjectPort, ProjectRepository projectRepository,
                                   UserIdentityLookupPort userIdentityLookupPort, UserRepository userRepository) {
        this.zepProjectPort = zepProjectPort;
        this.projectRepository = projectRepository;
        this.userIdentityLookupPort = userIdentityLookupPort;
        this.userRepository = userRepository;
    }

    @Override
    public ProjectLeadSyncResult sync() {
        SyncAccumulator syncAccumulator = syncProjects(projectRepository.findAll());
        persistProjects(syncAccumulator.projectsToSave());
        RoleReconciliationResult roleReconciliationResult = reconcileUserRoles(syncAccumulator.allLeadUserIds());

        return new ProjectLeadSyncResult(
                syncAccumulator.resolved(),
                syncAccumulator.skipped(),
                roleReconciliationResult.rolesAdded(),
                roleReconciliationResult.rolesRevoked()
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

    private RoleReconciliationResult reconcileUserRoles(Set<UserId> allLeadUserIds) {
        List<User> usersToUpdate = userRepository.findAll().stream()
                .filter(user -> {
                    boolean isLead = allLeadUserIds.contains(user.id());
                    boolean hasLeadRole = user.roles().contains(Role.PROJECT_LEAD);
                    return isLead != hasLeadRole;
                })
                .map(user -> updateLeadRole(user, allLeadUserIds.contains(user.id())))
                .toList();

        int rolesAdded = 0;
        int rolesRevoked = 0;

        for (User user : usersToUpdate) {
            if (user.roles().contains(Role.PROJECT_LEAD)) {
                rolesAdded++;
            } else {
                rolesRevoked++;
            }
        }

        if (!usersToUpdate.isEmpty()) {
            userRepository.saveAll(usersToUpdate);
        }
        return new RoleReconciliationResult(rolesAdded, rolesRevoked);
    }

    private User updateLeadRole(User user, boolean shouldBeLead) {
        Set<Role> updatedRoles = new HashSet<>(user.roles());
        if (shouldBeLead) {
            updatedRoles.add(Role.PROJECT_LEAD);
        } else {
            updatedRoles.remove(Role.PROJECT_LEAD);
        }
        return user.withRoles(updatedRoles);
    }

    private record ProjectLeadResolution(
            Project project,
            Set<UserId> resolvedLeadIds,
            int resolved,
            int skipped,
            boolean changed
    ) {
    }

    private record RoleReconciliationResult(int rolesAdded, int rolesRevoked) {
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
