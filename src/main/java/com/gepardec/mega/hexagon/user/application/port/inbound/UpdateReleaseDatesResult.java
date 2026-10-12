package com.gepardec.mega.hexagon.user.application.port.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.util.List;
import java.util.Objects;

public record UpdateReleaseDatesResult(List<UserId> failedUserIds) {

    public UpdateReleaseDatesResult {
        failedUserIds = List.copyOf(Objects.requireNonNull(failedUserIds, "failedUserIds must not be null"));
    }
}
