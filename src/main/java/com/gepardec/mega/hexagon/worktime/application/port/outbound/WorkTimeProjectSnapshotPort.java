package com.gepardec.mega.hexagon.worktime.application.port.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface WorkTimeProjectSnapshotPort {

    Optional<ProjectRef> findByZepId(int zepId, YearMonth month);

    List<ProjectRef> findAllByLead(UserId leadId, YearMonth month);

    /**
     * Resolves whether each given ZEP project is billable, regardless of whether it is active.
     * Projects unknown to MEGA are omitted from the result.
     */
    Map<Integer, Boolean> findBillabilityByZepIds(Set<Integer> zepIds);
}
