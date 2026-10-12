package com.gepardec.mega.hexagon.project.application.port.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.util.Objects;
import java.util.Set;

public record ProjectLeadSyncResult(int resolved, int skipped, Set<UserId> leadUserIds) {

    public ProjectLeadSyncResult {
        leadUserIds = Set.copyOf(Objects.requireNonNull(leadUserIds, "leadUserIds must not be null"));
    }
}
