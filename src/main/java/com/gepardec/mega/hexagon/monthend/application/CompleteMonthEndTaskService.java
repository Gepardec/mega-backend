package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.CompleteMonthEndTaskUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
public class CompleteMonthEndTaskService implements CompleteMonthEndTaskUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;

    @Inject
    public CompleteMonthEndTaskService(MonthEndTaskRepository monthEndTaskRepository) {
        this.monthEndTaskRepository = monthEndTaskRepository;
    }

    @Override
    public MonthEndTask complete(MonthEndTaskId taskId, UserId actorId) {
        MonthEndTask task = monthEndTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("month-end task not found: " + taskId.value()));

        MonthEndTask completedTask = task.complete(actorId);
        if (!completedTask.equals(task)) {
            monthEndTaskRepository.save(completedTask);
            Log.infof("Task %s completed by actor %s", taskId.value(), actorId.value());
        }

        return completedTask;
    }
}
