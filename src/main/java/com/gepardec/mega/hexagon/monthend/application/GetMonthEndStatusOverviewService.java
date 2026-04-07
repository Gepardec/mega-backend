package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
public class GetMonthEndStatusOverviewService implements GetMonthEndStatusOverviewUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;
    private final ResolveMonthEndTaskSnapshotLookupService resolveMonthEndTaskSnapshotLookupService;
    private final MonthEndStatusOverviewMapper monthEndStatusOverviewMapper;

    @Inject
    public GetMonthEndStatusOverviewService(
            MonthEndTaskRepository monthEndTaskRepository,
            ResolveMonthEndTaskSnapshotLookupService resolveMonthEndTaskSnapshotLookupService,
            MonthEndStatusOverviewMapper monthEndStatusOverviewMapper
    ) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.resolveMonthEndTaskSnapshotLookupService = resolveMonthEndTaskSnapshotLookupService;
        this.monthEndStatusOverviewMapper = monthEndStatusOverviewMapper;
    }

    @Override
    public MonthEndStatusOverview getOverview(UserId actorId, YearMonth month) {
        List<MonthEndTask> tasks = monthEndTaskRepository.findTasksForActor(actorId, month);
        if (tasks.isEmpty()) {
            return new MonthEndStatusOverview(actorId, month, List.of());
        }

        MonthEndTaskSnapshotLookup snapshotLookup = resolveMonthEndTaskSnapshotLookupService.resolve(tasks);

        List<MonthEndStatusOverviewItem> entries = tasks.stream()
                .map(task -> monthEndStatusOverviewMapper.toItem(
                        task,
                        snapshotLookup.projectFor(task.projectId()),
                        snapshotLookup.subjectEmployeeFor(task.subjectEmployeeId())
                ))
                .toList();
        return new MonthEndStatusOverview(actorId, month, entries);
    }
}
