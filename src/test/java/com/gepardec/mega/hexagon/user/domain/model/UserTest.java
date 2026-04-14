package com.gepardec.mega.hexagon.user.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void create_setsFieldsFromSyncData() {
        UserId id = UserId.generate();
        EmploymentPeriods employmentPeriods = new EmploymentPeriods(
                new EmploymentPeriod(LocalDate.of(2024, 1, 1), null)
        );

        User user = User.create(
                id,
                new ZepEmployeeSyncData(ZepUsername.of("jdoe"), "john@example.com", "John", "Doe", employmentPeriods),
                Set.of(Role.EMPLOYEE)
        );

        assertThat(user.id()).isEqualTo(id);
        assertThat(user.zepUsername()).isEqualTo(ZepUsername.of("jdoe"));
        assertThat(user.roles()).containsExactly(Role.EMPLOYEE);
        assertThat(user.name().firstname()).isEqualTo("John");
        assertThat(user.name().lastname()).isEqualTo("Doe");
        assertThat(user.email().value()).isEqualTo("john@example.com");
        assertThat(user.personioId()).isNull();
        assertThat(user.employmentPeriods()).isEqualTo(employmentPeriods);
    }

    @Test
    void withSyncedZepData_updatesIdentityFieldsAndEmploymentHistory() {
        User user = User.create(
                UserId.generate(),
                new ZepEmployeeSyncData(ZepUsername.of("jdoe"), "john@example.com", "John", "Doe", EmploymentPeriods.empty()),
                Set.of(Role.EMPLOYEE)
        );

        EmploymentPeriods updatedPeriods = new EmploymentPeriods(
                new EmploymentPeriod(LocalDate.of(2024, 1, 1), null)
        );

        User updated = user.withSyncedZepData(
                new ZepEmployeeSyncData(ZepUsername.of("jdoe"), "new@example.com", "Jane", "Smith", updatedPeriods),
                Set.of(Role.EMPLOYEE, Role.OFFICE_MANAGEMENT)
        );

        assertThat(updated.id()).isEqualTo(user.id());
        assertThat(updated.name()).isEqualTo(FullName.of("Jane", "Smith"));
        assertThat(updated.email()).isEqualTo(Email.of("new@example.com"));
        assertThat(updated.employmentPeriods()).isEqualTo(updatedPeriods);
        assertThat(updated.roles()).containsExactlyInAnyOrder(Role.EMPLOYEE, Role.OFFICE_MANAGEMENT);
    }

    @Test
    void withPersonioId_setsStableReference() {
        User user = User.create(
                UserId.generate(),
                new ZepEmployeeSyncData(ZepUsername.of("jdoe"), "john@example.com", "John", "Doe", EmploymentPeriods.empty()),
                Set.of(Role.EMPLOYEE)
        );

        User updated = user.withPersonioId(PersonioId.of(42));

        assertThat(updated.personioId()).isEqualTo(PersonioId.of(42));
        assertThat(user.personioId()).isNull();
    }

    @Test
    void isActiveOn_andIsActiveIn_deriveStateFromEmploymentPeriods() {
        User user = User.create(
                UserId.generate(),
                new ZepEmployeeSyncData(
                        ZepUsername.of("jdoe"),
                        "john@example.com",
                        "John",
                        "Doe",
                        new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30)))
                ),
                Set.of(Role.EMPLOYEE)
        );

        assertThat(user.isActiveOn(LocalDate.of(2024, 3, 15))).isTrue();
        assertThat(user.isActiveOn(LocalDate.of(2024, 7, 1))).isFalse();
        assertThat(user.isActiveIn(YearMonth.of(2024, 6))).isTrue();
        assertThat(user.isActiveIn(YearMonth.of(2024, 7))).isFalse();
    }
}
