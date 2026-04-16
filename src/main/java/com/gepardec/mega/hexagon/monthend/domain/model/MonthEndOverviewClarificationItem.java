package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.Instant;
import java.util.Objects;

public record MonthEndOverviewClarificationItem(
        MonthEndClarificationId clarificationId,
        ProjectId projectId,
        UserId subjectEmployeeId,
        UserId createdBy,
        MonthEndClarificationSide creatorSide,
        MonthEndClarificationStatus status,
        String text,
        boolean canResolve,
        String resolutionNote,
        UserId resolvedBy,
        Instant resolvedAt,
        Instant createdAt,
        Instant lastModifiedAt
) {

    public MonthEndOverviewClarificationItem {
        Objects.requireNonNull(clarificationId, "clarificationId must not be null");
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(subjectEmployeeId, "subjectEmployeeId must not be null");
        Objects.requireNonNull(createdBy, "createdBy must not be null");
        Objects.requireNonNull(creatorSide, "creatorSide must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(lastModifiedAt, "lastModifiedAt must not be null");
    }
}
