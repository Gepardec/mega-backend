package com.gepardec.mega.hexagon.user.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public record User(
        UserId id,
        Email email,
        FullName name,
        ZepUsername zepUsername,
        PersonioId personioId,
        EmploymentPeriods employmentPeriods,
        Set<Role> roles
) {

    public User {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(zepUsername, "zepUsername must not be null");
        Objects.requireNonNull(employmentPeriods, "employmentPeriods must not be null");
        roles = Set.copyOf(Objects.requireNonNull(roles, "roles must not be null"));
    }

    public static User create(UserId id, ZepEmployeeSyncData syncData, Set<Role> roles) {
        return new User(
                id,
                Email.of(syncData.email()),
                FullName.of(syncData.firstname(), syncData.lastname()),
                syncData.zepUsername(),
                null,
                syncData.employmentPeriods(),
                roles
        );
    }

    public User withSyncedZepData(ZepEmployeeSyncData syncData, Set<Role> roles) {
        return new User(
                id,
                Email.of(syncData.email()),
                FullName.of(syncData.firstname(), syncData.lastname()),
                syncData.zepUsername(),
                personioId,
                syncData.employmentPeriods(),
                roles
        );
    }

    public User withPersonioId(PersonioId newPersonioId) {
        if (newPersonioId == null || Objects.equals(personioId, newPersonioId)) {
            return this;
        }
        return new User(id, email, name, zepUsername, newPersonioId, employmentPeriods, roles);
    }

    public User withRoles(Set<Role> updatedRoles) {
        return new User(id, email, name, zepUsername, personioId, employmentPeriods, updatedRoles);
    }

    public User grantProjectLeadRole() {
        if (roles.contains(Role.PROJECT_LEAD)) {
            return this;
        }

        Set<Role> updatedRoles = mutableRoles();
        updatedRoles.add(Role.PROJECT_LEAD);
        return withRoles(updatedRoles);
    }

    public User revokeProjectLeadRole() {
        if (!roles.contains(Role.PROJECT_LEAD)) {
            return this;
        }

        Set<Role> updatedRoles = mutableRoles();
        updatedRoles.remove(Role.PROJECT_LEAD);
        return withRoles(updatedRoles);
    }

    private Set<Role> mutableRoles() {
        Set<Role> updatedRoles = EnumSet.noneOf(Role.class);
        updatedRoles.addAll(roles);
        return updatedRoles;
    }

    public boolean isActiveOn(LocalDate referenceDate) {
        return employmentPeriods.isActive(referenceDate);
    }

    public boolean isActiveIn(YearMonth payrollMonth) {
        return employmentPeriods.isActive(payrollMonth);
    }

    public boolean hasPersonioId() {
        return personioId != null;
    }
}
