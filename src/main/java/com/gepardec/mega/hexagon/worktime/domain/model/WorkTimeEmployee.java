package com.gepardec.mega.hexagon.worktime.domain.model;

import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.util.Objects;

public record WorkTimeEmployee(
        UserId id,
        String name
) {

    public WorkTimeEmployee {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}
