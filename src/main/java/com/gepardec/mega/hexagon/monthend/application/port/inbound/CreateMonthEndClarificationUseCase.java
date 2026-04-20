package com.gepardec.mega.hexagon.monthend.application.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.SourceSystem;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;

public interface CreateMonthEndClarificationUseCase {

    default MonthEndClarification create(
            YearMonth month,
            ProjectId projectId,
            UserId subjectEmployeeId,
            UserId actorId,
            String text
    ) {
        return create(month, projectId, subjectEmployeeId, actorId, SourceSystem.MEGA, text);
    }

    MonthEndClarification create(
            YearMonth month,
            ProjectId projectId,
            UserId subjectEmployeeId,
            UserId actorId,
            SourceSystem sourceSystem,
            String text
    );
}
