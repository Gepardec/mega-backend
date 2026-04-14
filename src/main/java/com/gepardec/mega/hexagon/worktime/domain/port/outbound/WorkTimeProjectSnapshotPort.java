package com.gepardec.mega.hexagon.worktime.domain.port.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface WorkTimeProjectSnapshotPort {

    Optional<ProjectRef> findByZepId(int zepId, YearMonth month);

    List<ProjectRef> findAllByLead(UserId leadId, YearMonth month);
}
