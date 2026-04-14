package com.gepardec.mega.hexagon.worktime.domain.port.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeUserSnapshot;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WorkTimeUserSnapshotPort {

    Optional<WorkTimeUserSnapshot> findById(UserId userId);

    List<WorkTimeUserSnapshot> findByZepUsernames(Set<ZepUsername> usernames);
}
