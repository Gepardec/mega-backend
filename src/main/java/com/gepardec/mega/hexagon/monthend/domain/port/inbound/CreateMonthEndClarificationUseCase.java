package com.gepardec.mega.hexagon.monthend.domain.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.time.YearMonth;

public interface CreateMonthEndClarificationUseCase {

    MonthEndClarification create(
            YearMonth month,
            ProjectId projectId,
            UserId subjectEmployeeId,
            UserId actorId,
            MonthEndClarificationSide creatorSide,
            String text
    );
}
