package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.util.Objects;
import java.util.Set;

public record MonthEndProjectSnapshot(
        ProjectId id,
        int zepId,
        String name,
        boolean billable,
        Set<UserId> leadIds
) {

    public MonthEndProjectSnapshot {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(leadIds, "leadIds must not be null");
        leadIds = Set.copyOf(leadIds);
    }
}
