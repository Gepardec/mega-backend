package com.gepardec.mega.hexagon.project.domain.model;

import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

public record Project(
        ProjectId id,
        int zepId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        boolean billable,
        Set<UserId> leads
) {

    public Project {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(startDate, "startDate must not be null");
        leads = Set.copyOf(Objects.requireNonNull(leads, "leads must not be null"));
    }

    public static Project create(ProjectId id, ZepProjectProfile profile) {
        return new Project(id, profile.zepId(), profile.name(), profile.startDate(), profile.endDate(), profile.billable(), Set.of());
    }

    public Project withSyncedZepData(ZepProjectProfile profile) {
        return new Project(id, profile.zepId(), profile.name(), profile.startDate(), profile.endDate(), profile.billable(), leads);
    }

    public Project withLeads(Set<UserId> updatedLeads) {
        return new Project(id, zepId, name, startDate, endDate, billable, updatedLeads);
    }
}
