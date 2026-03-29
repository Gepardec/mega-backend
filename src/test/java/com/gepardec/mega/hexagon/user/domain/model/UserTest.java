package com.gepardec.mega.hexagon.user.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private static ZepProfile zepProfile(String username, String email) {
        return new ZepProfile(username, email, "John", "Doe", null, null, null, null, null, EmploymentPeriods.empty(), RegularWorkingTimes.empty());
    }

    private static ZepProfile zepProfile(String username) {
        return zepProfile(username, username + "@example.com");
    }

    @Test
    void create_setsFieldsFromZepProfile() {
        UserId id = UserId.generate();
        ZepProfile profile = zepProfile("jdoe", "john@example.com");

        User user = User.create(id, profile, Set.of(Role.EMPLOYEE));

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getZepProfile()).isEqualTo(profile);
        assertThat(user.getRoles()).containsExactly(Role.EMPLOYEE);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getName().firstname()).isEqualTo("John");
        assertThat(user.getName().lastname()).isEqualTo("Doe");
        assertThat(user.getEmail().value()).isEqualTo("john@example.com");
        assertThat(user.getPersonioProfile()).isNull();
    }

    @Test
    void syncFromZep_updatesZepProfileAndName() {
        User user = User.create(UserId.generate(), zepProfile("jdoe"), Set.of(Role.EMPLOYEE));
        ZepProfile updated = new ZepProfile("jdoe", "new@example.com", "Jane", "Smith", null, null, null, null, null, EmploymentPeriods.empty(), RegularWorkingTimes.empty());

        user.syncFromZep(updated);

        assertThat(user.getZepProfile()).isEqualTo(updated);
        assertThat(user.getName().firstname()).isEqualTo("Jane");
        assertThat(user.getName().lastname()).isEqualTo("Smith");
        assertThat(user.getEmail().value()).isEqualTo("new@example.com");
    }

    @Test
    void syncFromZep_doesNotAffectPersonioProfile() {
        User user = User.create(UserId.generate(), zepProfile("jdoe"), Set.of(Role.EMPLOYEE));
        PersonioProfile personioProfile = new PersonioProfile(42, 10.0, "guild", "project", false);
        user.syncFromPersonio(personioProfile);

        user.syncFromZep(zepProfile("jdoe"));

        assertThat(user.getPersonioProfile()).isEqualTo(personioProfile);
    }

    @Test
    void syncFromPersonio_setsProfile() {
        User user = User.create(UserId.generate(), zepProfile("jdoe"), Set.of(Role.EMPLOYEE));
        PersonioProfile profile = new PersonioProfile(42, 10.0, "guild", "project", true);

        user.syncFromPersonio(profile);

        assertThat(user.getPersonioProfile()).isEqualTo(profile);
    }

    @Test
    void syncFromPersonio_doesNotClearExistingProfileWhenNull() {
        User user = User.create(UserId.generate(), zepProfile("jdoe"), Set.of(Role.EMPLOYEE));
        PersonioProfile existing = new PersonioProfile(42, 10.0, "guild", "project", false);
        user.syncFromPersonio(existing);

        user.syncFromPersonio(null);

        assertThat(user.getPersonioProfile()).isEqualTo(existing);
    }

    @Test
    void deactivate_setsStatusInactive() {
        User user = User.create(UserId.generate(), zepProfile("jdoe"), Set.of(Role.EMPLOYEE));

        user.deactivate();

        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void activate_setsStatusActive() {
        User user = User.create(UserId.generate(), zepProfile("jdoe"), Set.of(Role.EMPLOYEE));
        user.deactivate();

        user.activate();

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}
