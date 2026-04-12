package com.gepardec.mega.hexagon.user.domain.model;

import java.time.LocalDate;
import java.time.YearMonth;
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
