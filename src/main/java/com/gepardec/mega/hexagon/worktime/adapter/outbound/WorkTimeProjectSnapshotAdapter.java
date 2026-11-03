package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeProjectSnapshotPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public Map<Integer, Boolean> findBillabilityByZepIds(Set<Integer> zepIds) {
        Objects.requireNonNull(zepIds, "zepIds must not be null");
        return zepIds.stream()
                .map(projectRepository::findByZepId)
                .flatMap(Optional::stream)
                .collect(Collectors.toUnmodifiableMap(Project::zepId, Project::billable));
    }
}
