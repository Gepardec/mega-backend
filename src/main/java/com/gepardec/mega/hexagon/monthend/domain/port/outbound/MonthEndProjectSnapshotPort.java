package com.gepardec.mega.hexagon.monthend.domain.port.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;

public interface MonthEndProjectSnapshotPort {

    List<MonthEndProjectSnapshot> findActiveIn(YearMonth month);

    List<MonthEndProjectSnapshot> findByIds(Set<ProjectId> projectIds, YearMonth month);
}
