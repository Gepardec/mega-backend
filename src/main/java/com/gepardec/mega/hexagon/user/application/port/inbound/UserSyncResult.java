package com.gepardec.mega.hexagon.user.application.port.inbound;

public record UserSyncResult(
        int added,
        int updated,
        int unchanged,
        int skippedNoEmail,
        int personioLinked
) {
}
