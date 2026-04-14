package com.gepardec.mega.hexagon.shared.domain.model;

import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.util.Objects;
import java.util.Set;

public record AuthenticatedActor(
        UserId userId,
        Email email,
        Set<Role> roles
) {

    public AuthenticatedActor {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(email, "email must not be null");
        roles = Set.copyOf(Objects.requireNonNull(roles, "roles must not be null"));
    }
}
