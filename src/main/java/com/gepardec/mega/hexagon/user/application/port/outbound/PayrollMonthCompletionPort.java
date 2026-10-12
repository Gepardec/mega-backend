package com.gepardec.mega.hexagon.user.application.port.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;
import java.util.Set;

public interface PayrollMonthCompletionPort {

    Set<UserId> findUsersWithAllTasksCompleted(YearMonth month);
}
