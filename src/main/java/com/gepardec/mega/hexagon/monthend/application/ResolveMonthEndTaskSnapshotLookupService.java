package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public MonthEndTaskSnapshotLookup resolve(List<MonthEndTask> tasks, YearMonth month) {
        Objects.requireNonNull(tasks, "tasks must not be null");
        return resolve(tasks, List.of(), month);
    }

    public MonthEndTaskSnapshotLookup resolve(
            List<MonthEndTask> tasks,
            List<MonthEndClarification> clarifications,
            YearMonth month
    ) {
        Objects.requireNonNull(tasks, "tasks must not be null");
        Objects.requireNonNull(clarifications, "clarifications must not be null");
        Objects.requireNonNull(month, "month must not be null");
        if (tasks.isEmpty() && clarifications.isEmpty()) {
            return MonthEndTaskSnapshotLookup.empty();
        }

        return new MonthEndTaskSnapshotLookup(
                tasks.isEmpty() ? Map.of() : findProjectSnapshotsById(tasks, month),
                findUserSnapshotsById(tasks, clarifications, month)
        );
    }

    private Map<ProjectId, ProjectRef> findProjectSnapshotsById(List<MonthEndTask> tasks, YearMonth month) {
        Set<ProjectId> projectIds = tasks.stream()
                .map(MonthEndTask::projectId)
                .collect(Collectors.toSet());
        return monthEndProjectSnapshotPort.findByIds(projectIds, month).stream()
                .collect(Collectors.toMap(
                        MonthEndProjectSnapshot::id,
                        snapshot -> new ProjectRef(snapshot.id(), snapshot.zepId(), snapshot.name())
                ));
    }

    private Map<UserId, UserRef> findUserSnapshotsById(
            List<MonthEndTask> tasks,
            List<MonthEndClarification> clarifications,
            YearMonth month
    ) {
        Set<UserId> userIds = Stream.of(
                        tasks.stream().map(MonthEndTask::subjectEmployeeId),
                        clarifications.stream().map(MonthEndClarification::subjectEmployeeId),
                        clarifications.stream().map(MonthEndClarification::createdBy),
                        clarifications.stream().map(MonthEndClarification::resolvedBy)
                )
                .flatMap(Function.identity())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return userIds.isEmpty()
                ? Map.of()
                : monthEndUserSnapshotPort.findByIds(userIds, month).stream()
                  .collect(Collectors.toMap(
                          UserRef::id,
                          Function.identity()
                  ));
    }
}
