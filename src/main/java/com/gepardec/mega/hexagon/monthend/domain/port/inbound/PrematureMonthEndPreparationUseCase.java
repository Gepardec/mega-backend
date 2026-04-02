package com.gepardec.mega.hexagon.monthend.domain.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndPreparationResult;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.time.YearMonth;

public interface PrematureMonthEndPreparationUseCase {

    MonthEndPreparationResult prepare(YearMonth month, ProjectId projectId, UserId actorId, String clarificationText);
}
