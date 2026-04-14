package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;
import java.util.Objects;

public record MonthEndTaskKey(
        YearMonth month,
        ProjectId projectId,
        MonthEndTaskType type,
        UserId subjectEmployeeId
) {

    public MonthEndTaskKey {
        Objects.requireNonNull(month, "month must not be null");
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(type, "type must not be null");
    }
}
