package com.gepardec.mega.hexagon.project.domain.port.inbound;

public record ProjectLeadSyncResult(int resolved, int skipped, int rolesAdded, int rolesRevoked) {
}
