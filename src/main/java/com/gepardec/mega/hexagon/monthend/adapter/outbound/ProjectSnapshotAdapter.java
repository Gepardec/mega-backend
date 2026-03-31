package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ProjectSnapshotAdapter implements MonthEndProjectSnapshotPort {

    @Inject
    ProjectRepository projectRepository;

    @Inject
    MonthEndProjectSnapshotMapper mapper;

    @Override
    public List<MonthEndProjectSnapshot> findAll() {
        return projectRepository.findAll().stream()
                .map(mapper::toSnapshot)
                .toList();
    }
}
