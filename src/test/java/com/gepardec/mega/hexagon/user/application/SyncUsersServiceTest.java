package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.PersonioProfile;
import com.gepardec.mega.hexagon.user.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.UserStatus;
import com.gepardec.mega.hexagon.user.domain.model.ZepProfile;
import com.gepardec.mega.hexagon.user.domain.port.outbound.PersonioEmployeePort;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.user.domain.port.outbound.ZepEmployeePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SyncUsersServiceTest {

    private ZepEmployeePort zepEmployeePort;
    private PersonioEmployeePort personioEmployeePort;
    private UserRepository userRepository;
    private SyncUsersService syncUsersService;

    @BeforeEach
    void setUp() {
        zepEmployeePort = mock(ZepEmployeePort.class);
        personioEmployeePort = mock(PersonioEmployeePort.class);
        userRepository = mock(UserRepository.class);
    }

    private SyncUsersService service(List<String> omUsernames) {
        return new SyncUsersService(zepEmployeePort, personioEmployeePort, userRepository,
                new UserSyncConfig(omUsernames));
    }

    private ZepProfile profile(String username) {
        return new ZepProfile(username, username + "@example.com", "John", "Doe", null, null, null, null, null, List.of(), List.of());
    }

    @Test
    void sync_createsNewUserForUnknownZepEmployee() {
        when(zepEmployeePort.fetchAll()).thenReturn(List.of(profile("jdoe")));
        when(userRepository.findAll()).thenReturn(List.of());
        when(personioEmployeePort.findByEmail(any())).thenReturn(Optional.empty());

        service(List.of()).sync();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users).hasSize(1);
            assertThat(users.getFirst().zepProfile().username()).isEqualTo("jdoe");
            assertThat(users.getFirst().status()).isEqualTo(UserStatus.ACTIVE);
            assertThat(users.getFirst().roles()).contains(Role.EMPLOYEE);
            return true;
        }));
    }

    @Test
    void sync_updatesExistingUserFromZep() {
        ZepProfile oldProfile = profile("jdoe");
        User existing = User.create(UserId.generate(), oldProfile, Set.of(Role.EMPLOYEE));
        ZepProfile newProfile = new ZepProfile("jdoe", "jdoe@example.com", "Jane", "Updated", null, null, null, null, null, List.of(), List.of());

        when(zepEmployeePort.fetchAll()).thenReturn(List.of(newProfile));
        when(userRepository.findAll()).thenReturn(List.of(existing));
        when(personioEmployeePort.findByEmail(any())).thenReturn(Optional.empty());

        service(List.of()).sync();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users).hasSize(1);
            assertThat(users.getFirst().name().firstname()).isEqualTo("Jane");
            assertThat(users.getFirst().name().lastname()).isEqualTo("Updated");
            return true;
        }));
    }

    @Test
    void sync_deactivatesUserAbsentFromZep() {
        User existing = User.create(UserId.generate(), profile("jdoe"), Set.of(Role.EMPLOYEE));

        when(zepEmployeePort.fetchAll()).thenReturn(List.of()); // jdoe is gone
        when(userRepository.findAll()).thenReturn(List.of(existing));

        service(List.of()).sync();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users.stream().filter(u -> u.zepProfile().username().equals("jdoe")).findFirst())
                    .hasValueSatisfying(u -> assertThat(u.status()).isEqualTo(UserStatus.INACTIVE));
            return true;
        }));
    }

    @Test
    void sync_assignsOfficeManagementRoleFromConfig() {
        when(zepEmployeePort.fetchAll()).thenReturn(List.of(profile("om_user")));
        when(userRepository.findAll()).thenReturn(List.of());
        when(personioEmployeePort.findByEmail(any())).thenReturn(Optional.empty());

        service(List.of("om_user")).sync();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users.getFirst().roles()).contains(Role.OFFICE_MANAGEMENT, Role.EMPLOYEE);
            return true;
        }));
    }

    @Test
    void sync_doesNotAssignOfficeManagementRoleForRegularEmployee() {
        when(zepEmployeePort.fetchAll()).thenReturn(List.of(profile("jdoe")));
        when(userRepository.findAll()).thenReturn(List.of());
        when(personioEmployeePort.findByEmail(any())).thenReturn(Optional.empty());

        service(List.of("om_user")).sync();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users.getFirst().roles()).doesNotContain(Role.OFFICE_MANAGEMENT);
            return true;
        }));
    }

    @Test
    void sync_enrichesUserWithPersonioDataWhenAvailable() {
        PersonioProfile personioProfile = new PersonioProfile(99, 15.0, "guild", "project", false);

        when(zepEmployeePort.fetchAll()).thenReturn(List.of(profile("jdoe")));
        when(userRepository.findAll()).thenReturn(List.of());
        when(personioEmployeePort.findByEmail(Email.of("jdoe@example.com"))).thenReturn(Optional.of(personioProfile));

        service(List.of()).sync();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users.getFirst().personioProfile()).isEqualTo(personioProfile);
            return true;
        }));
    }

    @Test
    void sync_preservesExistingPersonioProfileWhenPersonioUnavailable() {
        PersonioProfile existing = new PersonioProfile(99, 15.0, "guild", "project", false);
        User user = User.reconstitute(UserId.generate(), Email.of("jdoe@example.com"),
                FullName.of("John", "Doe"), UserStatus.ACTIVE, Set.of(Role.EMPLOYEE), profile("jdoe"), existing);

        when(zepEmployeePort.fetchAll()).thenReturn(List.of(profile("jdoe")));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(personioEmployeePort.findByEmail(any())).thenReturn(Optional.empty());

        service(List.of()).sync();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users.getFirst().personioProfile()).isEqualTo(existing);
            return true;
        }));
    }

    @Test
    void sync_continuesWhenPersonioUnavailableForOneUser() {
        when(zepEmployeePort.fetchAll()).thenReturn(List.of(profile("user1"), profile("user2")));
        when(userRepository.findAll()).thenReturn(List.of());
        when(personioEmployeePort.findByEmail(Email.of("user1@example.com"))).thenReturn(Optional.empty());
        when(personioEmployeePort.findByEmail(Email.of("user2@example.com")))
                .thenReturn(Optional.of(new PersonioProfile(42, 5.0, null, null, false)));

        service(List.of()).sync();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users).hasSize(2);
            return true;
        }));
    }
}
