package com.gepardec.mega.hexagon.monthend.application.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

public interface CompleteMonthEndClarificationUseCase {

    MonthEndClarification complete(MonthEndClarificationId clarificationId, UserId actorId, String resolutionNote);
}
