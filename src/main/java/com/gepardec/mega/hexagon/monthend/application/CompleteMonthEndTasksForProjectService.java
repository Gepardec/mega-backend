package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndTasksForProjectUseCase;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndActorNotAuthorizedException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectContext;
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
public class CompleteMonthEndTasksForProjectService implements CompleteMonthEndTasksForProjectUseCase {
    private final MonthEndTaskRepository monthEndTaskRepository;
    private final MonthEndProjectContextService monthEndProjectContextService;

    @Inject
    public CompleteMonthEndTasksForProjectService(
            MonthEndTaskRepository monthEndTaskRepository,
            MonthEndProjectContextService monthEndProjectContextService
    ) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.monthEndProjectContextService = monthEndProjectContextService;
    }


    @Override
    public List<MonthEndTask> complete(YearMonth month, ProjectId projectId, MonthEndTaskType type, UserId actorId) {
        MonthEndProjectContext monthEndProjectContext = monthEndProjectContextService.resolve(month, projectId);
        if(!monthEndProjectContext.eligibleProjectLeadIds().contains(actorId)) {
            throw new MonthEndActorNotAuthorizedException("actor not authorized: " + actorId.value());
        }

        List<MonthEndTask> tasksToUpdate = monthEndTaskRepository.findByProjectMonthAndType(month, projectId, type)
                .stream()
                .filter(task -> task.isOpen() && task.canBeCompletedBy(actorId) )
                .toList();

        tasksToUpdate.forEach(task -> task.complete(actorId));
        monthEndTaskRepository.saveAll(tasksToUpdate);

        Log.infof("Completed %i month-end tasks for month %s, project %s, type %s by actor %s",
                tasksToUpdate.size(), month, projectId.value(), type.name(), actorId.value());

        return tasksToUpdate;
    }
}
