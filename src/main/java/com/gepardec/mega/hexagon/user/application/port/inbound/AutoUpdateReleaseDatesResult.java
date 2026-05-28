package com.gepardec.mega.hexagon.user.application.port.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public record AutoUpdateReleaseDatesResult(
        YearMonth payrollMonth,
        LocalDate releaseDate,
        List<UserId> updatedUserIds,
        List<UserId> failedUserIds
) {

    public AutoUpdateReleaseDatesResult {
        Objects.requireNonNull(payrollMonth, "payrollMonth must not be null");
        Objects.requireNonNull(releaseDate, "releaseDate must not be null");
        updatedUserIds = List.copyOf(Objects.requireNonNull(updatedUserIds, "updatedUserIds must not be null"));
        failedUserIds = List.copyOf(Objects.requireNonNull(failedUserIds, "failedUserIds must not be null"));
    }
}
