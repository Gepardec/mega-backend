package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.application.port.inbound.SyncProjectLeadRolesResult;
import com.gepardec.mega.hexagon.user.application.port.inbound.SyncProjectLeadRolesUseCase;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.user.domain.services.UserRolePolicyService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
@Transactional
public class SyncProjectLeadRolesService implements SyncProjectLeadRolesUseCase {

    private final UserRepository userRepository;
    private final UserRolePolicyService userRolePolicyService;

    @Inject
    public SyncProjectLeadRolesService(
            UserRepository userRepository,
            UserRolePolicyService userRolePolicyService
    ) {
        this.userRepository = userRepository;
        this.userRolePolicyService = userRolePolicyService;
    }

    @Override
    public SyncProjectLeadRolesResult sync(Set<UserId> leadUserIds) {
        List<User> usersToUpdate = userRepository.findAll().stream()
                .map(user -> userRolePolicyService.updateProjectLeadRoleIfNeeded(user, leadUserIds.contains(user.id())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        int rolesAdded = (int) usersToUpdate.stream()
                .filter(user -> leadUserIds.contains(user.id()))
                .count();
        int rolesRevoked = usersToUpdate.size() - rolesAdded;

        if (!usersToUpdate.isEmpty()) {
            userRepository.saveAll(usersToUpdate);
        }

        return new SyncProjectLeadRolesResult(rolesAdded, rolesRevoked);
    }
}
