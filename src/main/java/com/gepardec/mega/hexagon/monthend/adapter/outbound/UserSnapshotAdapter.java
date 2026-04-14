package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class UserSnapshotAdapter implements MonthEndUserSnapshotPort {

    @Inject
    UserRepository userRepository;

    @Inject
    MonthEndUserSnapshotMapper mapper;

    @Override
    public List<UserRef> findActiveIn(YearMonth month) {
        return userRepository.findAll().stream()
                .filter(user -> user.isActiveIn(month))
                .map(mapper::toSnapshot)
                .toList();
    }

    @Override
    public List<UserRef> findByIds(Set<UserId> userIds, YearMonth month) {
        return userRepository.findByIds(userIds).stream()
                .filter(user -> user.isActiveIn(month))
                .map(mapper::toSnapshot)
                .toList();
    }
}
