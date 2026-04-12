package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.PersonioId;
import com.gepardec.mega.hexagon.user.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepEmployeeSyncData;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.port.inbound.UserSyncResult;
import com.gepardec.mega.hexagon.user.domain.port.outbound.PersonioEmployeePort;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.user.domain.port.outbound.ZepEmployeePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SyncUsersServiceTest {

    private ZepEmployeePort zepEmployeePort;
    private PersonioEmployeePort personioEmployeePort;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        zepEmployeePort = mock(ZepEmployeePort.class);
        personioEmployeePort = mock(PersonioEmployeePort.class);
        userRepository = mock(UserRepository.class);
    }

    @Test
    void sync_createsNewUserForUnknownZepEmployee() {
        when(zepEmployeePort.fetchAll()).thenReturn(List.of(syncData("jdoe")));
        when(userRepository.findByZepUsernames(anySet())).thenReturn(List.of());
        when(personioEmployeePort.findPersonioIdByEmail(any())).thenReturn(Optional.empty());

        service(List.of()).sync();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users).hasSize(1);
            assertThat(users.getFirst().zepUsername()).isEqualTo(ZepUsername.of("jdoe"));
            assertThat(users.getFirst().roles()).containsExactly(Role.EMPLOYEE);
            assertThat(users.getFirst().employmentPeriods().employmentPeriods()).hasSize(1);
            return true;
        }));
    }

    @Test
    void sync_updatesExistingUserAndPreservesProjectLeadRole() {
        User existing = user("jdoe", "old@example.com", "John", "Old", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD), null);
        ZepEmployeeSyncData updatedSyncData = syncData("jdoe", "jdoe@example.com", "Jane", "Updated", activeEmployment());

        when(zepEmployeePort.fetchAll()).thenReturn(List.of(updatedSyncData));
        when(userRepository.findByZepUsernames(Set.of(ZepUsername.of("jdoe")))).thenReturn(List.of(existing));
        when(personioEmployeePort.findPersonioIdByEmail(any())).thenReturn(Optional.empty());

        service(List.of()).sync();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users).hasSize(1);
            assertThat(users.getFirst().name()).isEqualTo(FullName.of("Jane", "Updated"));
            assertThat(users.getFirst().roles()).containsExactlyInAnyOrder(Role.EMPLOYEE, Role.PROJECT_LEAD);
            return true;
        }));
    }

    @Test
    void sync_assignsOfficeManagementRoleByConfiguredEmail() {
        when(zepEmployeePort.fetchAll()).thenReturn(List.of(syncData("om", "om@example.com", "Om", "User", activeEmployment())));
        when(userRepository.findByZepUsernames(anySet())).thenReturn(List.of());
        when(personioEmployeePort.findPersonioIdByEmail(any())).thenReturn(Optional.empty());

        service(List.of("om@example.com")).sync();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users.getFirst().roles()).containsExactlyInAnyOrder(Role.EMPLOYEE, Role.OFFICE_MANAGEMENT);
            return true;
        }));
    }

    @Test
    void sync_retainsHistoricalEmployeesAndCountsSkippedUsersWithoutEmail() {
        ZepEmployeeSyncData endedEmployment = syncData(
                "former",
                "former@example.com",
                "Former",
                "Employee",
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.now().minusYears(2), LocalDate.now().minusYears(1)))
        );
        ZepEmployeeSyncData missingEmail = syncData("missing", null, "Missing", "Mail", activeEmployment());

        when(zepEmployeePort.fetchAll()).thenReturn(List.of(endedEmployment, missingEmail));
        when(userRepository.findByZepUsernames(Set.of(ZepUsername.of("former")))).thenReturn(List.of());
        when(personioEmployeePort.findPersonioIdByEmail(any())).thenReturn(Optional.empty());

        UserSyncResult result = service(List.of()).sync();

        verify(userRepository).saveAll(argThat(users -> users.size() == 1
                && users.getFirst().zepUsername().equals(ZepUsername.of("former"))));
        assertThat(result.added()).isEqualTo(1);
        assertThat(result.skippedNoEmail()).isEqualTo(1);
    }

    @Test
    void sync_linksMissingPersonioIdAndCountsSupplementalMetric() {
        when(zepEmployeePort.fetchAll()).thenReturn(List.of(syncData("jdoe")));
        when(userRepository.findByZepUsernames(anySet())).thenReturn(List.of());
        when(personioEmployeePort.findPersonioIdByEmail(Email.of("jdoe@example.com"))).thenReturn(Optional.of(PersonioId.of(99)));

        UserSyncResult result = service(List.of()).sync();

        verify(userRepository).saveAll(argThat(users -> {
            assertThat(users.getFirst().personioId()).isEqualTo(PersonioId.of(99));
            return true;
        }));
        assertThat(result.personioLinked()).isEqualTo(1);
        assertThat(result.added()).isEqualTo(1);
    }

    @Test
    void sync_skipsPersonioLookupWhenUserIsAlreadyLinked() {
        User existing = user("jdoe", "jdoe@example.com", "John", "Doe", Set.of(Role.EMPLOYEE), PersonioId.of(42));

        when(zepEmployeePort.fetchAll()).thenReturn(List.of(syncData("jdoe")));
        when(userRepository.findByZepUsernames(Set.of(ZepUsername.of("jdoe")))).thenReturn(List.of(existing));

        UserSyncResult result = service(List.of()).sync();

        verify(personioEmployeePort, never()).findPersonioIdByEmail(any());
        assertThat(result.updated()).isZero();
        assertThat(result.unchanged()).isEqualTo(1);
    }

    @Test
    void sync_result_countsAddedUpdatedUnchangedSkippedAndPersonioLinked() {
        User unchanged = user("unchanged", "unchanged@example.com", "Stable", "User", Set.of(Role.EMPLOYEE), PersonioId.of(7));
        User updated = user("updated", "updated@example.com", "Old", "Name", Set.of(Role.EMPLOYEE), null);

        when(zepEmployeePort.fetchAll()).thenReturn(List.of(
                syncData("new"),
                syncData("updated", "updated@example.com", "New", "Name", activeEmployment()),
                syncData("unchanged", "unchanged@example.com", "Stable", "User", activeEmployment()),
                syncData("missing", null, "No", "Email", activeEmployment())
        ));
        when(userRepository.findByZepUsernames(Set.of(
                ZepUsername.of("new"),
                ZepUsername.of("updated"),
                ZepUsername.of("unchanged")
        ))).thenReturn(List.of(updated, unchanged));
        when(personioEmployeePort.findPersonioIdByEmail(Email.of("new@example.com"))).thenReturn(Optional.of(PersonioId.of(99)));
        when(personioEmployeePort.findPersonioIdByEmail(Email.of("updated@example.com"))).thenReturn(Optional.empty());

        UserSyncResult result = service(List.of()).sync();

        assertThat(result.added()).isEqualTo(1);
        assertThat(result.updated()).isEqualTo(1);
        assertThat(result.unchanged()).isEqualTo(1);
        assertThat(result.skippedNoEmail()).isEqualTo(1);
        assertThat(result.personioLinked()).isEqualTo(1);
    }

    private SyncUsersService service(List<String> omEmails) {
        return new SyncUsersService(zepEmployeePort, personioEmployeePort, userRepository, omEmails);
    }

    private ZepEmployeeSyncData syncData(String username) {
        return syncData(username, username + "@example.com", "John", "Doe", activeEmployment());
    }

    private ZepEmployeeSyncData syncData(String username, String email, String firstname, String lastname, EmploymentPeriods employmentPeriods) {
        return new ZepEmployeeSyncData(ZepUsername.of(username), email, firstname, lastname, employmentPeriods);
    }

    private EmploymentPeriods activeEmployment() {
        return new EmploymentPeriods(new EmploymentPeriod(LocalDate.now().minusYears(1), null));
    }

    private User user(String username, String email, String firstname, String lastname, Set<Role> roles, PersonioId personioId) {
        return new User(
                UserId.generate(),
                Email.of(email),
                FullName.of(firstname, lastname),
                ZepUsername.of(username),
                personioId,
                activeEmployment(),
                roles
        );
    }
}
