package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.application.port.inbound.SyncUsersUseCase;
import com.gepardec.mega.hexagon.user.application.port.inbound.UserSyncResult;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.ZepEmployeeSyncData;
import com.gepardec.mega.hexagon.user.domain.port.outbound.PersonioEmployeePort;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.user.domain.port.outbound.ZepEmployeePort;
import com.gepardec.mega.hexagon.user.domain.services.UserRolePolicyService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class SyncUsersService implements SyncUsersUseCase {

    private final ZepEmployeePort zepEmployeePort;
    private final PersonioEmployeePort personioEmployeePort;
    private final UserRepository userRepository;
    private final UserRolePolicyService userRolePolicyService;

    @Inject
    public SyncUsersService(ZepEmployeePort zepEmployeePort, PersonioEmployeePort personioEmployeePort,
                            UserRepository userRepository,
                            UserRolePolicyService userRolePolicyService) {
        this.zepEmployeePort = zepEmployeePort;
        this.personioEmployeePort = personioEmployeePort;
        this.userRepository = userRepository;
        this.userRolePolicyService = userRolePolicyService;
    }

    @Override
    public UserSyncResult sync() {
        SyncInput syncInput = fetchSyncInput();
        Map<ZepUsername, User> existingByUsername = findExistingUsers(syncInput.users());
        SyncAccumulator syncAccumulator = synchronizeUsers(syncInput.users(), existingByUsername);

        persistChanges(syncAccumulator.usersToSave());

        return new UserSyncResult(
                syncAccumulator.added(),
                syncAccumulator.updated(),
                syncAccumulator.unchanged(),
                syncInput.skippedNoEmail(),
                syncAccumulator.personioLinked()
        );
    }

    private SyncInput fetchSyncInput() {
        List<ZepEmployeeSyncData> zepEmployees = zepEmployeePort.fetchAll();
        int skippedNoEmail = (int) zepEmployees.stream()
                .filter(this::hasNoEmail)
                .count();

        List<ZepEmployeeSyncData> usersWithEmail = zepEmployees.stream()
                .filter(user -> !hasNoEmail(user))
                .toList();

        return new SyncInput(usersWithEmail, skippedNoEmail);
    }

    private Map<ZepUsername, User> findExistingUsers(List<ZepEmployeeSyncData> syncInput) {
        Set<ZepUsername> zepUsernames = syncInput.stream()
                .map(ZepEmployeeSyncData::zepUsername)
                .collect(Collectors.toSet());

        return userRepository.findByZepUsernames(zepUsernames).stream()
                .collect(Collectors.toMap(User::zepUsername, Function.identity()));
    }

    private SyncAccumulator synchronizeUsers(List<ZepEmployeeSyncData> zepEmployees, Map<ZepUsername, User> existingByUsername) {
        SyncAccumulator syncAccumulator = new SyncAccumulator();
        for (ZepEmployeeSyncData zepEmployee : zepEmployees) {
            syncAccumulator.recordUser(processUser(zepEmployee, existingByUsername.get(zepEmployee.zepUsername())));
        }
        return syncAccumulator;
    }

    private ProcessedUser processUser(ZepEmployeeSyncData zepEmployee, User existingUser) {
        User synchronizedUser = synchronizeUser(zepEmployee, existingUser);
        User enrichedUser = enrichWithPersonioId(synchronizedUser);

        boolean personioLinked = !synchronizedUser.hasPersonioId() && enrichedUser.hasPersonioId();
        ChangeType changeType = determineChangeType(existingUser, enrichedUser);

        return new ProcessedUser(enrichedUser, changeType, personioLinked);
    }

    private User synchronizeUser(ZepEmployeeSyncData zepEmployee, User existingUser) {
        Set<Role> roles = userRolePolicyService.determineRoles(zepEmployee.email(), existingUser);
        if (existingUser != null) {
            return existingUser.withSyncedZepData(zepEmployee, roles);
        }
        return User.create(UserId.generate(), zepEmployee, roles);
    }

    private User enrichWithPersonioId(User user) {
        if (user.hasPersonioId()) {
            return user;
        }

        return personioEmployeePort.findPersonioIdByEmail(user.email())
                .map(user::withPersonioId)
                .orElse(user);
    }

    private ChangeType determineChangeType(User existingUser, User updatedUser) {
        if (existingUser == null) {
            return ChangeType.ADDED;
        }
        if (!Objects.equals(existingUser, updatedUser)) {
            return ChangeType.UPDATED;
        }
        return ChangeType.UNCHANGED;
    }

    private void persistChanges(List<User> usersToSave) {
        if (!usersToSave.isEmpty()) {
            userRepository.saveAll(usersToSave);
        }
    }

    private boolean hasNoEmail(ZepEmployeeSyncData profile) {
        return profile.email() == null || profile.email().isBlank();
    }

    private record SyncInput(List<ZepEmployeeSyncData> users, int skippedNoEmail) {
    }

    private record ProcessedUser(User user, ChangeType changeType, boolean personioLinked) {
    }

    private enum ChangeType {
        ADDED,
        UPDATED,
        UNCHANGED
    }

    private static final class SyncAccumulator {

        private final List<User> usersToSave = new ArrayList<>();
        private int added;
        private int updated;
        private int unchanged;
        private int personioLinked;

        void recordUser(ProcessedUser processedUser) {
            if (processedUser.personioLinked()) {
                personioLinked++;
            }

            switch (processedUser.changeType()) {
                case ADDED -> {
                    added++;
                    usersToSave.add(processedUser.user());
                }
                case UPDATED -> {
                    updated++;
                    usersToSave.add(processedUser.user());
                }
                case UNCHANGED -> unchanged++;
            }
        }

        List<User> usersToSave() {
            return usersToSave;
        }

        int added() {
            return added;
        }

        int updated() {
            return updated;
        }

        int unchanged() {
            return unchanged;
        }

        int personioLinked() {
            return personioLinked;
        }
    }
}
