package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.port.inbound.ReconcileLeadsUseCase;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.project.domain.port.outbound.UserLookupPort;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ZepProjectPort;
import com.gepardec.mega.hexagon.user.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public void reconcile() {
        List<Project> projects = projectRepository.findAll();

        Set<UUID> allLeadUserIds = new HashSet<>();

        for (Project project : projects) {
            List<String> leadUsernames = zepProjectPort.fetchLeadUsernames(project.zepId());
            Set<UUID> resolvedLeads = leadUsernames.stream()
                    .map(userLookupPort::findUserIdByZepUsername)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
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
                .toList();

        for (User user : usersToUpdate) {
            boolean shouldBeLead = allLeadUserIds.contains(user.id().value());
            Set<Role> updatedRoles = new HashSet<>(user.roles());
            if (shouldBeLead) {
                updatedRoles.add(Role.PROJECT_LEAD);
            } else {
                updatedRoles.remove(Role.PROJECT_LEAD);
            }
            user.setRoles(updatedRoles);
        }

        if (!usersToUpdate.isEmpty()) {
            userRepository.saveAll(usersToUpdate);
        }
    }
}
