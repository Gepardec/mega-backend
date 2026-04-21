package com.gepardec.mega.hexagon.monthend.domain.event;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.SourceSystem;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

public record ClarificationCreatedEvent(
        MonthEndClarificationId clarificationId,
        SourceSystem sourceSystem,
        UserId creator,
        UserId subjectEmployeeId,
        String text
) {
}
