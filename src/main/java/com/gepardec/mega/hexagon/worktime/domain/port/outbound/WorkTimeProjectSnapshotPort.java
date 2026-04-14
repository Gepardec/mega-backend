package com.gepardec.mega.hexagon.worktime.domain.port.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeProjectSnapshot;

import java.util.List;
import java.util.Optional;

public interface WorkTimeProjectSnapshotPort {

    Optional<WorkTimeProjectSnapshot> findByZepId(int zepId);

    List<WorkTimeProjectSnapshot> findAllByLead(UserId leadId);
}
