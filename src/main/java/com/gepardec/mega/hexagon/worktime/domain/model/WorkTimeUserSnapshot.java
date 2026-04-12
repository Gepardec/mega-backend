package com.gepardec.mega.hexagon.worktime.domain.model;

import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.util.Objects;

public record WorkTimeUserSnapshot(
        UserId id,
        String fullName,
        String zepUsername
) {

    public WorkTimeUserSnapshot {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(fullName, "fullName must not be null");
    }
}
