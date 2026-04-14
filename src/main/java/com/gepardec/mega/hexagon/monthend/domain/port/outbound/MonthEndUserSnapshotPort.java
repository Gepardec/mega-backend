package com.gepardec.mega.hexagon.monthend.domain.port.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.util.List;
import java.util.Set;

public interface MonthEndUserSnapshotPort {

    List<MonthEndUserSnapshot> findAll();

    List<MonthEndUserSnapshot> findByIds(Set<UserId> userIds);
}
