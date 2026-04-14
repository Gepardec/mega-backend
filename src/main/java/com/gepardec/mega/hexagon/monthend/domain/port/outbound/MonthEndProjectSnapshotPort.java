package com.gepardec.mega.hexagon.monthend.domain.port.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;

import java.util.List;
import java.util.Set;

public interface MonthEndProjectSnapshotPort {

    List<MonthEndProjectSnapshot> findAll();

    List<MonthEndProjectSnapshot> findByIds(Set<ProjectId> projectIds);
}
