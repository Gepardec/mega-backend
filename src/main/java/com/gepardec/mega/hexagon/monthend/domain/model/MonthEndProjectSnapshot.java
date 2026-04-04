package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Set;

public record MonthEndProjectSnapshot(
        ProjectId id,
        int zepId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        boolean billable,
        Set<UserId> leadIds
) {

    public MonthEndProjectSnapshot {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(startDate, "startDate must not be null");
        Objects.requireNonNull(leadIds, "leadIds must not be null");
        leadIds = Set.copyOf(leadIds);
    }

    public boolean isActiveIn(YearMonth month) {
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();
        boolean startsBeforeMonthEnds = !startDate.isAfter(monthEnd);
        boolean endsAfterMonthStarts = endDate == null || !endDate.isBefore(monthStart);
        return startsBeforeMonthEnds && endsAfterMonthStarts;
    }
}
