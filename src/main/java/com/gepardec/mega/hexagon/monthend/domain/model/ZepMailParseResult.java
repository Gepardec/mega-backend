package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;

import java.util.Optional;

public record ZepMailParseResult(
        Optional<ZepProjektzeitEntry> entry,
        Optional<ZepUsername> creatorUsername
) {
}
