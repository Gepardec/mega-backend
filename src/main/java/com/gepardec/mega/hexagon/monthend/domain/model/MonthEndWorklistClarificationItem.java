package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.Instant;

public record MonthEndWorklistClarificationItem(
        MonthEndClarificationId clarificationId,
        ProjectId projectId,
        UserId subjectEmployeeId,
        UserId createdBy,
        MonthEndClarificationSide creatorSide,
        MonthEndClarificationStatus status,
        String text,
        Instant createdAt,
        Instant lastModifiedAt
) {
}
