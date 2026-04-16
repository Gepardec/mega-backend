package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndOverviewClarificationItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class AssembleMonthEndStatusOverviewService {

    private final ResolveMonthEndTaskSnapshotLookupService resolveMonthEndTaskSnapshotLookupService;

    @Inject
    public AssembleMonthEndStatusOverviewService(
            ResolveMonthEndTaskSnapshotLookupService resolveMonthEndTaskSnapshotLookupService
    ) {
        this.resolveMonthEndTaskSnapshotLookupService = resolveMonthEndTaskSnapshotLookupService;
    }

    public MonthEndStatusOverview assemble(
            List<MonthEndTask> tasks,
            List<MonthEndClarification> clarifications,
            UserId actorId,
            YearMonth month
    ) {
        Objects.requireNonNull(tasks, "tasks must not be null");
        Objects.requireNonNull(clarifications, "clarifications must not be null");
        Objects.requireNonNull(actorId, "actorId must not be null");
        Objects.requireNonNull(month, "month must not be null");

        MonthEndTaskSnapshotLookup snapshotLookup = resolveMonthEndTaskSnapshotLookupService
                .resolve(tasks, clarifications, month);

        List<MonthEndStatusOverviewItem> entries = tasks.stream()
                .map(task -> new MonthEndStatusOverviewItem(
                        task.id(),
                        task.type(),
                        task.status(),
                        snapshotLookup.projectFor(task.projectId()),
                        snapshotLookup.userFor(task.subjectEmployeeId()),
                        task.eligibleActorIds().contains(actorId),
                        task.completedBy()
                ))
                .toList();

        List<MonthEndOverviewClarificationItem> clarificationItems = clarifications.stream()
                .map(clarification -> new MonthEndOverviewClarificationItem(
                        clarification.id(),
                        clarification.projectId(),
                        snapshotLookup.userFor(clarification.subjectEmployeeId()),
                        snapshotLookup.userFor(clarification.createdBy()),
                        clarification.creatorSide(),
                        clarification.status(),
                        clarification.text(),
                        clarification.canBeResolvedBy(actorId),
                        clarification.resolutionNote(),
                        snapshotLookup.userFor(clarification.resolvedBy()),
                        clarification.resolvedAt(),
                        clarification.createdAt(),
                        clarification.lastModifiedAt()
                ))
                .toList();

        return new MonthEndStatusOverview(actorId, month, entries, clarificationItems);
    }
}
