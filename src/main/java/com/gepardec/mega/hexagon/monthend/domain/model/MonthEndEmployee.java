package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.util.Objects;

public record MonthEndEmployee(
        UserId id,
        String fullName
) {

    public MonthEndEmployee {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(fullName, "fullName must not be null");
    }
}
