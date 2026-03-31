package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

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
}
