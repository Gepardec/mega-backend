package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteOwnTimeCheckTasksUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
@Transactional
public class CompleteOwnTimeCheckTasksService implements CompleteOwnTimeCheckTasksUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;

    @Inject
    public CompleteOwnTimeCheckTasksService(MonthEndTaskRepository monthEndTaskRepository) {
        this.monthEndTaskRepository = monthEndTaskRepository;
    }

    @Override
    public List<MonthEndTask> complete(UserId actorId, YearMonth month, ProjectId projectId) {
        List<MonthEndTask> openTasks = monthEndTaskRepository
                .findOpenEmployeeTimeCheckTasks(actorId, month, projectId)
                .stream()
                .filter(task -> task.isOpen() && task.canBeCompletedBy(actorId))
                .toList();

        List<MonthEndTask> completedTasks = openTasks.stream()
                .map(task -> task.complete(actorId))
                .toList();

        monthEndTaskRepository.saveAll(completedTasks);

        Log.infof("Completed %d time-check tasks for employee %s in month %s",
                completedTasks.size(), actorId.value(), month);

        return completedTasks;
    }
}