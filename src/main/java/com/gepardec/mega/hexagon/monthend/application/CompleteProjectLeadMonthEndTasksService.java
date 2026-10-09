package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteProjectLeadMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndErrorCode;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
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
public class CompleteProjectLeadMonthEndTasksService implements CompleteProjectLeadMonthEndTasksUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;

    @Inject
    public CompleteProjectLeadMonthEndTasksService(MonthEndTaskRepository monthEndTaskRepository) {
        this.monthEndTaskRepository = monthEndTaskRepository;
    }

    @Override
    public List<MonthEndTask> complete(YearMonth month, ProjectId projectId, MonthEndTaskType type, UserId actorId) {
        if (!type.isProjectLeadBulkCompletable()) {
            throw new MonthEndException(MonthEndErrorCode.VALIDATION_FAILED, "task type %s cannot be bulk completed by a project lead".formatted(type.name()));
        }
        if (!monthEndTaskRepository.existsLeadTask(month, projectId, actorId)) {
            throw new MonthEndException(MonthEndErrorCode.ACTOR_NOT_AUTHORIZED, "actor not authorized: " + actorId.value());
        }

        List<MonthEndTask> completedTasks = monthEndTaskRepository.findOpenProjectTasksOfType(month, projectId, type)
                .stream()
                .filter(task -> task.isOpen() && task.canBeCompletedBy(actorId))
                .map(task -> task.complete(actorId))
                .toList();

        monthEndTaskRepository.saveAll(completedTasks);

        Log.infof("Completed %d month-end tasks for month %s, project %s, type %s by actor %s",
                completedTasks.size(), month, projectId.value(), type.name(), actorId.value());

        return completedTasks;
    }
}
