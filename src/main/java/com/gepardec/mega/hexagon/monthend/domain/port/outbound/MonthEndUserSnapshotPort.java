package com.gepardec.mega.hexagon.monthend.domain.port.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;

public interface MonthEndUserSnapshotPort {

    List<UserRef> findActiveIn(YearMonth month);

    List<UserRef> findByIds(Set<UserId> userIds, YearMonth month);
}
