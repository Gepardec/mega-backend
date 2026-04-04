package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Set;

@ApplicationScoped
public class UserSnapshotAdapter implements MonthEndUserSnapshotPort {

    @Inject
    UserRepository userRepository;

    @Inject
    MonthEndUserSnapshotMapper mapper;

    @Override
    public List<MonthEndUserSnapshot> findAll() {
        return userRepository.findAll().stream()
                .map(mapper::toSnapshot)
                .toList();
    }

    @Override
    public List<MonthEndUserSnapshot> findByIds(Set<UserId> userIds) {
        return userRepository.findByIds(userIds).stream()
                .map(mapper::toSnapshot)
                .toList();
    }
}
