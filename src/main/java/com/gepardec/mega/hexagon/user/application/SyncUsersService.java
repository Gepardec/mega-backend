package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.user.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepProfile;
import com.gepardec.mega.hexagon.user.domain.port.inbound.SyncUsersUseCase;
import com.gepardec.mega.hexagon.user.domain.port.inbound.UserSyncResult;
import com.gepardec.mega.hexagon.user.domain.port.outbound.PersonioEmployeePort;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.user.domain.port.outbound.ZepEmployeePort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SyncUsersService implements SyncUsersUseCase {

    private final ZepEmployeePort zepEmployeePort;
    private final PersonioEmployeePort personioEmployeePort;
    private final UserRepository userRepository;
    private final UserSyncConfig userSyncConfig;

    public SyncUsersService(ZepEmployeePort zepEmployeePort, PersonioEmployeePort personioEmployeePort,
                            UserRepository userRepository, UserSyncConfig userSyncConfig) {
        this.zepEmployeePort = zepEmployeePort;
        this.personioEmployeePort = personioEmployeePort;
        this.userRepository = userRepository;
        this.userSyncConfig = userSyncConfig;
    }

    @Override
    public UserSyncResult sync() {
        LocalDate today = LocalDate.now();
        List<ZepProfile> zepProfiles = zepEmployeePort.fetchAll().stream()
                .filter(p -> p.email() != null && p.employmentPeriods().active(today).isPresent())
                .toList();
        Set<String> zepUsernames = zepProfiles.stream()
                .map(ZepProfile::username)
                .collect(Collectors.toSet());

        Map<String, User> existingByUsername = userRepository.findAll().stream()
                .filter(u -> u.getZepProfile() != null)
                .collect(Collectors.toMap(u -> u.getZepProfile().username(), Function.identity()));

        List<User> toSave = new ArrayList<>();
        int added = 0;
        int updated = 0;

        // Create or update users from ZEP
        for (ZepProfile zepProfile : zepProfiles) {
            User user;
            if (existingByUsername.containsKey(zepProfile.username())) {
                user = existingByUsername.get(zepProfile.username());
                user.syncFromZep(zepProfile);
                updated++;
            } else {
                Set<Role> roles = EnumSet.of(Role.EMPLOYEE);
                user = User.create(UserId.generate(), zepProfile, roles);
                added++;
            }

            user.activate();
            user.setRoles(buildRoles(zepProfile.username()));
            toSave.add(user);
        }

        // Best-effort Personio enrichment
        for (User user : toSave) {
            if (user.getEmail() != null && user.getEmail().value() != null) {
                personioEmployeePort.findByEmail(user.getEmail())
                        .ifPresent(user::syncFromPersonio);
            }
        }

        // Deactivate users absent from ZEP
        int disabled = 0;
        for (User existing : existingByUsername.values()) {
            if (!zepUsernames.contains(existing.getZepProfile().username())) {
                existing.deactivate();
                toSave.add(existing);
                disabled++;
            }
        }

        userRepository.saveAll(toSave);
        return new UserSyncResult(added, updated, disabled);
    }

    private Set<Role> buildRoles(String username) {
        Set<Role> roles = EnumSet.of(Role.EMPLOYEE);
        if (userSyncConfig.officeManagementUsernames().contains(username)) {
            roles.add(Role.OFFICE_MANAGEMENT);
        }
        return roles;
    }
}
