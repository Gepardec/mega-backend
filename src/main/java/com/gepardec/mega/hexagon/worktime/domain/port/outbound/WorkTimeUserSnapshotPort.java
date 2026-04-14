package com.gepardec.mega.hexagon.worktime.domain.port.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WorkTimeUserSnapshotPort {

    Optional<UserRef> findById(UserId userId, YearMonth month);

    List<UserRef> findByZepUsernames(Set<ZepUsername> usernames, YearMonth month);
}
