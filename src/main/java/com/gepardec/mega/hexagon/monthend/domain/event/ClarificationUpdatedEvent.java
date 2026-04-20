package com.gepardec.mega.hexagon.monthend.domain.event;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

public record ClarificationUpdatedEvent(
        MonthEndClarificationId clarificationId,
        UserId actorId,
        UserId subjectEmployeeId,
        String text
) {
}
