package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.UserStatus;

import java.time.YearMonth;
import java.util.Objects;

public record MonthEndUserSnapshot(
        UserId id,
        String fullName,
        String zepUsername,
        UserStatus status,
        EmploymentPeriods employmentPeriods
) {

    public MonthEndUserSnapshot {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(fullName, "fullName must not be null");
        Objects.requireNonNull(zepUsername, "zepUsername must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(employmentPeriods, "employmentPeriods must not be null");
    }

    public boolean isActiveIn(YearMonth month) {
        return status == UserStatus.ACTIVE && employmentPeriods.active(month).isPresent();
    }
}
