package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistClarificationItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistItem;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetEmployeeMonthEndWorklistUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
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
public class GetEmployeeMonthEndWorklistService implements GetEmployeeMonthEndWorklistUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;
    private final MonthEndClarificationRepository monthEndClarificationRepository;
    private final MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private final MonthEndUserSnapshotPort monthEndUserSnapshotPort;
    private final MonthEndWorklistMapper monthEndWorklistMapper;

    @Inject
    public GetEmployeeMonthEndWorklistService(
            MonthEndTaskRepository monthEndTaskRepository,
            MonthEndClarificationRepository monthEndClarificationRepository,
            MonthEndProjectSnapshotPort monthEndProjectSnapshotPort,
            MonthEndUserSnapshotPort monthEndUserSnapshotPort,
            MonthEndWorklistMapper monthEndWorklistMapper
    ) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.monthEndClarificationRepository = monthEndClarificationRepository;
        this.monthEndProjectSnapshotPort = monthEndProjectSnapshotPort;
        this.monthEndUserSnapshotPort = monthEndUserSnapshotPort;
        this.monthEndWorklistMapper = monthEndWorklistMapper;
    }

    @Override
    public MonthEndWorklist getWorklist(UserId employeeId, YearMonth month) {
        List<MonthEndTask> tasks = monthEndTaskRepository.findOpenEmployeeTasks(employeeId, month);

        List<MonthEndWorklistItem> worklistItems = List.of();
        if (!tasks.isEmpty()) {
            Map<ProjectId, MonthEndProjectSnapshot> projectsById = findProjectSnapshotsById(tasks);
            Map<UserId, MonthEndUserSnapshot> usersById = findUserSnapshotsById(tasks);
            worklistItems = tasks.stream()
                    .map(task -> monthEndWorklistMapper.toItem(
                            task,
                            projectFor(task.projectId(), projectsById),
                            subjectEmployeeFor(task.subjectEmployeeId(), usersById)
                    ))
                    .toList();
        }

        List<MonthEndWorklistClarificationItem> clarifications = monthEndClarificationRepository
                .findOpenEmployeeClarifications(employeeId, month).stream()
                .map(monthEndWorklistMapper::toItem)
                .toList();
        return new MonthEndWorklist(employeeId, month, worklistItems, clarifications);
    }

    private Map<ProjectId, MonthEndProjectSnapshot> findProjectSnapshotsById(List<MonthEndTask> tasks) {
        Set<ProjectId> projectIds = tasks.stream()
                .map(MonthEndTask::projectId)
                .collect(Collectors.toSet());
        return monthEndProjectSnapshotPort.findByIds(projectIds).stream()
                .collect(Collectors.toMap(MonthEndProjectSnapshot::id, Function.identity()));
    }

    private Map<UserId, MonthEndUserSnapshot> findUserSnapshotsById(List<MonthEndTask> tasks) {
        Set<UserId> subjectEmployeeIds = tasks.stream()
                .map(MonthEndTask::subjectEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return subjectEmployeeIds.isEmpty()
                ? Map.of()
                : monthEndUserSnapshotPort.findByIds(subjectEmployeeIds).stream()
                  .collect(Collectors.toMap(MonthEndUserSnapshot::id, Function.identity()));
    }

    private MonthEndProjectSnapshot projectFor(ProjectId projectId, Map<ProjectId, MonthEndProjectSnapshot> projectsById) {
        MonthEndProjectSnapshot project = projectsById.get(projectId);
        if (project == null) {
            throw new IllegalStateException("project snapshot not found for project %s".formatted(projectId.value()));
        }
        return project;
    }

    private MonthEndUserSnapshot subjectEmployeeFor(UserId subjectEmployeeId, Map<UserId, MonthEndUserSnapshot> usersById) {
        if (subjectEmployeeId == null) {
            return null;
        }
        MonthEndUserSnapshot user = usersById.get(subjectEmployeeId);
        if (user == null) {
            throw new IllegalStateException("subject employee snapshot not found for employee %s"
                    .formatted(subjectEmployeeId.value()));
        }
        return user;
    }
}
