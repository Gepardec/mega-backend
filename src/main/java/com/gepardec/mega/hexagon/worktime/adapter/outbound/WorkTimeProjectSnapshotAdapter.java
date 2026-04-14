package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeProjectSnapshotPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
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
    public Optional<ProjectRef> findByZepId(int zepId, YearMonth month) {
        return projectRepository.findByZepId(zepId)
                .filter(project -> project.isActiveIn(month))
                .map(mapper::toSnapshot);
    }

    @Override
    public List<ProjectRef> findAllByLead(UserId leadId, YearMonth month) {
        return projectRepository.findAllByLead(leadId).stream()
                .filter(project -> project.isActiveIn(month))
                .map(mapper::toSnapshot)
                .toList();
    }
}
