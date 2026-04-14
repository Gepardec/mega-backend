package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeProjectSnapshot;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeProjectSnapshotPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class WorkTimeProjectSnapshotAdapter implements WorkTimeProjectSnapshotPort {

    private final ProjectRepository projectRepository;
    private final WorkTimeProjectSnapshotMapper mapper;

    @Inject
    public WorkTimeProjectSnapshotAdapter(ProjectRepository projectRepository, WorkTimeProjectSnapshotMapper mapper) {
        this.projectRepository = projectRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<WorkTimeProjectSnapshot> findByZepId(int zepId) {
        return projectRepository.findByZepId(zepId)
                .map(mapper::toSnapshot);
    }

    @Override
    public List<WorkTimeProjectSnapshot> findAllByLead(UserId leadId) {
        return projectRepository.findAllByLead(leadId).stream()
                .map(mapper::toSnapshot)
                .toList();
    }
}
