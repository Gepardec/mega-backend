package com.gepardec.mega.hexagon.project.application.port.inbound;

public record ProjectLeadSyncResult(int resolved, int skipped, int rolesAdded, int rolesRevoked) {
}
