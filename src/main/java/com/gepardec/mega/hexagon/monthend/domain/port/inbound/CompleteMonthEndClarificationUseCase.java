package com.gepardec.mega.hexagon.monthend.domain.port.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;

public interface CompleteMonthEndClarificationUseCase {

    MonthEndClarification complete(MonthEndClarificationId clarificationId, UserId actorId, String resolutionNote);
}
