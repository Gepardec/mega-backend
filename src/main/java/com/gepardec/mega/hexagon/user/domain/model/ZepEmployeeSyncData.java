package com.gepardec.mega.hexagon.user.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;

import java.util.Objects;

public record ZepEmployeeSyncData(
        ZepUsername zepUsername,
        String email,
        String firstname,
        String lastname,
        EmploymentPeriods employmentPeriods
) {

    public ZepEmployeeSyncData {
        Objects.requireNonNull(zepUsername, "zepUsername must not be null");
        Objects.requireNonNull(employmentPeriods, "employmentPeriods must not be null");
    }
}
