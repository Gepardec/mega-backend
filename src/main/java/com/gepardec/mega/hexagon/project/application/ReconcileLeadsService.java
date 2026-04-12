package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.port.inbound.ReconcileLeadsResult;
import com.gepardec.mega.hexagon.project.domain.port.inbound.ReconcileLeadsUseCase;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.project.domain.port.outbound.UserLookupPort;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ZepProjectPort;
import com.gepardec.mega.hexagon.user.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ReconcileLeadsService implements ReconcileLeadsUseCase {

    private final ZepProjectPort zepProjectPort;
    private final ProjectRepository projectRepository;
    private final UserLookupPort userLookupPort;
    private final UserRepository userRepository;

    public ReconcileLeadsService(ZepProjectPort zepProjectPort, ProjectRepository projectRepository,
                                 UserLookupPort userLookupPort, UserRepository userRepository) {
        this.zepProjectPort = zepProjectPort;
        this.projectRepository = projectRepository;
        this.userLookupPort = userLookupPort;
        this.userRepository = userRepository;
    }

    @Override
    public ReconcileLeadsResult reconcile() {
        List<Project> projects = projectRepository.findAll();

        Set<UUID> allLeadUserIds = new HashSet<>();
        int resolved = 0;
        int skipped = 0;

        for (Project project : projects) {
            List<String> leadUsernames = zepProjectPort.fetchLeadUsernames(project.getZepId());
            Set<UUID> resolvedLeads = new HashSet<>();
            for (String username : leadUsernames) {
                Optional<UUID> userId = userLookupPort.findUserIdByZepUsername(ZepUsername.of(username));
                if (userId.isPresent()) {
                    resolvedLeads.add(userId.get());
                    resolved++;
                } else {
                    skipped++;
                }
            }
            project.setLeads(resolvedLeads);
            allLeadUserIds.addAll(resolvedLeads);
        }

        projectRepository.saveAll(projects);

        List<User> allUsers = userRepository.findAll();
        List<User> usersToUpdate = allUsers.stream()
                .filter(user -> {
                    boolean isLead = allLeadUserIds.contains(user.id().value());
                    boolean hasLeadRole = user.roles().contains(Role.PROJECT_LEAD);
                    return isLead != hasLeadRole;
                })
                .map(user -> updateLeadRole(user, allLeadUserIds.contains(user.id().value())))
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

        return new ReconcileLeadsResult(resolved, skipped, rolesAdded, rolesRevoked);
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
}
