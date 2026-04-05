package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetMonthEndStatusOverviewService implements GetMonthEndStatusOverviewUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;
    private final MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private final MonthEndUserSnapshotPort monthEndUserSnapshotPort;
    private final MonthEndStatusOverviewMapper monthEndStatusOverviewMapper;

    @Inject
    public GetMonthEndStatusOverviewService(
            MonthEndTaskRepository monthEndTaskRepository,
            MonthEndProjectSnapshotPort monthEndProjectSnapshotPort,
            MonthEndUserSnapshotPort monthEndUserSnapshotPort,
            MonthEndStatusOverviewMapper monthEndStatusOverviewMapper
    ) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.monthEndProjectSnapshotPort = monthEndProjectSnapshotPort;
        this.monthEndUserSnapshotPort = monthEndUserSnapshotPort;
        this.monthEndStatusOverviewMapper = monthEndStatusOverviewMapper;
    }

    @Override
    public MonthEndStatusOverview getOverview(UserId actorId, YearMonth month) {
        List<MonthEndTask> tasks = monthEndTaskRepository.findTasksForActor(actorId, month);
        if (tasks.isEmpty()) {
            return new MonthEndStatusOverview(actorId, month, List.of());
        }

        Map<ProjectId, MonthEndProjectSnapshot> projectsById = findProjectSnapshotsById(tasks);
        Map<UserId, MonthEndUserSnapshot> usersById = findUsersById(tasks);

        List<MonthEndStatusOverviewItem> entries = tasks.stream()
                .map(task -> monthEndStatusOverviewMapper.toItem(
                        task,
                        projectFor(task.projectId(), projectsById),
                        subjectEmployeeFor(task.subjectEmployeeId(), usersById)
                ))
                .toList();
        return new MonthEndStatusOverview(actorId, month, entries);
    }

    private Map<UserId, MonthEndUserSnapshot> findUsersById(List<MonthEndTask> tasks) {
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

    private MonthEndProjectSnapshot projectFor(ProjectId projectId, Map<ProjectId, MonthEndProjectSnapshot> projectsById) {
        MonthEndProjectSnapshot project = projectsById.get(projectId);
        if (project == null) {
            throw new IllegalStateException("project snapshot not found for project %s".formatted(projectId.value()));
        }
        return project;
    }

    private MonthEndUserSnapshot subjectEmployeeFor(
            UserId subjectEmployeeId,
            Map<UserId, MonthEndUserSnapshot> subjectEmployeesById
    ) {
        if (subjectEmployeeId == null) {
            return null;
        }

        MonthEndUserSnapshot subjectEmployee = subjectEmployeesById.get(subjectEmployeeId);
        if (subjectEmployee == null) {
            throw new IllegalStateException("subject employee snapshot not found for employee %s"
                    .formatted(subjectEmployeeId.value()));
        }
        return subjectEmployee;
    }
}
