package com.gepardec.mega.hexagon.monthend.domain.event;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

public record ClarificationCompletedEvent(
        MonthEndClarificationId clarificationId,
        UserId creator,
        UserId subjectEmployeeId,
        String text,
        UserId resolver
) {
}
