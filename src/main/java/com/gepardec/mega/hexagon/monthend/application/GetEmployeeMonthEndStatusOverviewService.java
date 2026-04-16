package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetEmployeeMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
@Transactional
public class GetEmployeeMonthEndStatusOverviewService implements GetEmployeeMonthEndStatusOverviewUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;
    private final ResolveMonthEndTaskSnapshotLookupService resolveMonthEndTaskSnapshotLookupService;

    @Inject
    public GetEmployeeMonthEndStatusOverviewService(
            MonthEndTaskRepository monthEndTaskRepository,
            ResolveMonthEndTaskSnapshotLookupService resolveMonthEndTaskSnapshotLookupService
    ) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.resolveMonthEndTaskSnapshotLookupService = resolveMonthEndTaskSnapshotLookupService;
    }

    @Override
    public MonthEndStatusOverview getOverview(UserId employeeId, YearMonth month) {
        List<MonthEndTask> tasks = monthEndTaskRepository.findEmployeeVisibleTasks(employeeId, month);
        if (tasks.isEmpty()) {
            return new MonthEndStatusOverview(employeeId, month, List.of());
        }

        MonthEndTaskSnapshotLookup snapshotLookup = resolveMonthEndTaskSnapshotLookupService.resolve(tasks, month);

        List<MonthEndStatusOverviewItem> entries = tasks.stream()
                .map(task -> new MonthEndStatusOverviewItem(
                        task.id(),
                        task.type(),
                        task.status(),
                        snapshotLookup.projectFor(task.projectId()),
                        snapshotLookup.subjectEmployeeFor(task.subjectEmployeeId()),
                        task.eligibleActorIds().contains(employeeId),
                        task.completedBy()
                ))
                .toList();
        return new MonthEndStatusOverview(employeeId, month, entries);
    }
}
