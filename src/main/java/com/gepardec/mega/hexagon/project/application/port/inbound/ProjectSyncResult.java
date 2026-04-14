package com.gepardec.mega.hexagon.project.application.port.inbound;

public record ProjectSyncResult(int created, int updated, int unchanged) {
}
