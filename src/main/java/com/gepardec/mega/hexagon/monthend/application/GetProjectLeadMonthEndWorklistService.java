package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetProjectLeadMonthEndWorklistUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistClarificationItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistItem;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
@Transactional
public class GetProjectLeadMonthEndWorklistService implements GetProjectLeadMonthEndWorklistUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;
    private final MonthEndClarificationRepository monthEndClarificationRepository;
    private final ResolveMonthEndTaskSnapshotLookupService resolveMonthEndTaskSnapshotLookupService;

    @Inject
    public GetProjectLeadMonthEndWorklistService(
            MonthEndTaskRepository monthEndTaskRepository,
            MonthEndClarificationRepository monthEndClarificationRepository,
            ResolveMonthEndTaskSnapshotLookupService resolveMonthEndTaskSnapshotLookupService
    ) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.monthEndClarificationRepository = monthEndClarificationRepository;
        this.resolveMonthEndTaskSnapshotLookupService = resolveMonthEndTaskSnapshotLookupService;
    }

    @Override
    public MonthEndWorklist getWorklist(UserId projectLeadId, YearMonth month) {
        List<MonthEndTask> tasks = monthEndTaskRepository.findOpenProjectLeadTasks(projectLeadId, month);

        MonthEndTaskSnapshotLookup snapshotLookup = resolveMonthEndTaskSnapshotLookupService.resolve(tasks, month);
        List<MonthEndWorklistItem> worklistItems = tasks.stream()
                .map(task -> new MonthEndWorklistItem(
                        task.id(),
                        task.type(),
                        snapshotLookup.projectFor(task.projectId()),
                        snapshotLookup.userFor(task.subjectEmployeeId())
                ))
                .toList();

        List<MonthEndWorklistClarificationItem> clarifications = monthEndClarificationRepository
                .findOpenProjectLeadClarifications(projectLeadId, month).stream()
                .map(c -> new MonthEndWorklistClarificationItem(
                        c.id(),
                        c.projectId(),
                        c.subjectEmployeeId(),
                        c.createdBy(),
                        c.creatorSide(),
                        c.status(),
                        c.text(),
                        c.createdAt(),
                        c.lastModifiedAt()
                ))
                .toList();
        return new MonthEndWorklist(projectLeadId, month, worklistItems, clarifications);
    }
}
