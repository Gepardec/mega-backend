package com.gepardec.mega.hexagon.shared.domain.model;

import java.util.Objects;

public record UserRef(
        UserId id,
        FullName fullName,
        ZepUsername zepUsername
) {

    public UserRef {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(fullName, "fullName must not be null");
        Objects.requireNonNull(zepUsername, "zepUsername must not be null");
    }
}
