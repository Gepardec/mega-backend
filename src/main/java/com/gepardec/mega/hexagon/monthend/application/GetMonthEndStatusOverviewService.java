package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetMonthEndStatusOverviewService implements GetMonthEndStatusOverviewUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;
    private final MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private final MonthEndStatusOverviewMapper monthEndStatusOverviewMapper;

    @Inject
    public GetMonthEndStatusOverviewService(
            MonthEndTaskRepository monthEndTaskRepository,
            MonthEndProjectSnapshotPort monthEndProjectSnapshotPort,
            MonthEndStatusOverviewMapper monthEndStatusOverviewMapper
    ) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.monthEndProjectSnapshotPort = monthEndProjectSnapshotPort;
        this.monthEndStatusOverviewMapper = monthEndStatusOverviewMapper;
    }

    @Override
    public MonthEndStatusOverview getOverview(UserId actorId, YearMonth month) {
        List<MonthEndTask> tasks = monthEndTaskRepository.findTasksForActor(actorId, month);
        if (tasks.isEmpty()) {
            return new MonthEndStatusOverview(actorId, month, List.of());
        }

        Set<ProjectId> projectIds = tasks.stream()
                .map(MonthEndTask::projectId)
                .collect(Collectors.toSet());
        Map<ProjectId, MonthEndProjectSnapshot> projectsById = monthEndProjectSnapshotPort.findByIds(projectIds).stream()
                .collect(Collectors.toMap(
                        MonthEndProjectSnapshot::id,
                        Function.identity()
                ));

        List<MonthEndStatusOverviewItem> entries = tasks.stream()
                .map(task -> monthEndStatusOverviewMapper.toItem(task, projectFor(task.projectId(), projectsById)))
                .toList();
        return new MonthEndStatusOverview(actorId, month, entries);
    }

    private MonthEndProjectSnapshot projectFor(ProjectId projectId, Map<ProjectId, MonthEndProjectSnapshot> projectsById) {
        MonthEndProjectSnapshot project = projectsById.get(projectId);
        if (project == null) {
            throw new IllegalStateException("project snapshot not found for project %s".formatted(projectId.value()));
        }
        return project;
    }
}
