package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class ResolveMonthEndTaskSnapshotLookupService {

    private final MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private final MonthEndUserSnapshotPort monthEndUserSnapshotPort;

    @Inject
    public ResolveMonthEndTaskSnapshotLookupService(
            MonthEndProjectSnapshotPort monthEndProjectSnapshotPort,
            MonthEndUserSnapshotPort monthEndUserSnapshotPort
    ) {
        this.monthEndProjectSnapshotPort = monthEndProjectSnapshotPort;
        this.monthEndUserSnapshotPort = monthEndUserSnapshotPort;
    }

    public MonthEndTaskSnapshotLookup resolve(List<MonthEndTask> tasks) {
        Objects.requireNonNull(tasks, "tasks must not be null");
        if (tasks.isEmpty()) {
            return MonthEndTaskSnapshotLookup.empty();
        }

        return new MonthEndTaskSnapshotLookup(
                findProjectSnapshotsById(tasks),
                findUserSnapshotsById(tasks)
        );
    }

    private Map<ProjectId, MonthEndProjectSnapshot> findProjectSnapshotsById(List<MonthEndTask> tasks) {
        Set<ProjectId> projectIds = tasks.stream()
                .map(MonthEndTask::projectId)
                .collect(Collectors.toSet());
        return monthEndProjectSnapshotPort.findByIds(projectIds).stream()
                .collect(Collectors.toMap(
                        MonthEndProjectSnapshot::id,
                        Function.identity()
                ));
    }

    private Map<UserId, MonthEndUserSnapshot> findUserSnapshotsById(List<MonthEndTask> tasks) {
        Set<UserId> subjectEmployeeIds = tasks.stream()
                .map(MonthEndTask::subjectEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return subjectEmployeeIds.isEmpty()
                ? Map.of()
                : monthEndUserSnapshotPort.findByIds(subjectEmployeeIds).stream()
                  .collect(Collectors.toMap(
                          MonthEndUserSnapshot::id,
                          Function.identity()
                  ));
    }
}
