package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeUserSnapshotPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class WorkTimeUserSnapshotAdapter implements WorkTimeUserSnapshotPort {

    private final UserRepository userRepository;
    private final WorkTimeUserSnapshotMapper mapper;

    @Inject
    public WorkTimeUserSnapshotAdapter(UserRepository userRepository, WorkTimeUserSnapshotMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<UserRef> findById(UserId userId, YearMonth month) {
        return userRepository.findById(userId)
                .filter(user -> user.isActiveIn(month))
                .map(mapper::toSnapshot);
    }

    @Override
    public List<UserRef> findByZepUsernames(Set<ZepUsername> usernames, YearMonth month) {
        return userRepository.findByZepUsernames(usernames).stream()
                .filter(user -> user.isActiveIn(month))
                .map(mapper::toSnapshot)
                .toList();
    }
}
