package com.gepardec.mega.hexagon.user.application.port.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.LocalDate;
import java.util.Objects;

public record UpdateReleaseDateCommand(UserId userId, LocalDate releaseDate) {

    public UpdateReleaseDateCommand {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(releaseDate, "releaseDate must not be null");
    }
}
