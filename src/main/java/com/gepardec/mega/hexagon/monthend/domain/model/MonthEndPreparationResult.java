package com.gepardec.mega.hexagon.monthend.domain.model;

import java.util.List;
import java.util.Objects;

public record MonthEndPreparationResult(
        List<MonthEndTask> ensuredTasks,
        MonthEndClarification clarification
) {

    public MonthEndPreparationResult {
        Objects.requireNonNull(ensuredTasks, "ensuredTasks must not be null");
        ensuredTasks = List.copyOf(ensuredTasks);
    }

    public boolean hasClarification() {
        return clarification != null;
    }
}
