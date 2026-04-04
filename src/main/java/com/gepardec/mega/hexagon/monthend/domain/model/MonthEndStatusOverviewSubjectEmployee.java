package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.util.Objects;

public record MonthEndStatusOverviewSubjectEmployee(
        UserId id,
        String fullName
) {

    public MonthEndStatusOverviewSubjectEmployee {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(fullName, "fullName must not be null");
    }
}
