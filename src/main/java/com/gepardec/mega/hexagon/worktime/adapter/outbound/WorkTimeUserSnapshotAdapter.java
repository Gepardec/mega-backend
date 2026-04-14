package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeUserSnapshot;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeUserSnapshotPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
    public Optional<WorkTimeUserSnapshot> findById(UserId userId) {
        return userRepository.findById(userId)
                .map(mapper::toSnapshot);
    }

    @Override
    public List<WorkTimeUserSnapshot> findByZepUsernames(Set<ZepUsername> usernames) {
        return userRepository.findByZepUsernames(usernames).stream()
                .map(mapper::toSnapshot)
                .toList();
    }
}
