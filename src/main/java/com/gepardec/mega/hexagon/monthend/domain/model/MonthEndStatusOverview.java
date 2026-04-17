package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public record MonthEndStatusOverview(
        UserId actorId,
        YearMonth month,
        List<MonthEndTask> tasks,
        List<MonthEndClarification> clarifications
) {

    public MonthEndStatusOverview {
        Objects.requireNonNull(actorId, "actorId must not be null");
        Objects.requireNonNull(month, "month must not be null");
        Objects.requireNonNull(tasks, "tasks must not be null");
        Objects.requireNonNull(clarifications, "clarifications must not be null");
        tasks = List.copyOf(tasks);
        clarifications = List.copyOf(clarifications);
    }
}
