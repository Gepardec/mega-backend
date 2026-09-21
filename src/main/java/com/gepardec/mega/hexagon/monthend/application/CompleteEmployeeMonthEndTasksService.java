package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteEmployeeMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
@Transactional
public class CompleteEmployeeMonthEndTasksService implements CompleteEmployeeMonthEndTasksUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;

    @Inject
    public CompleteEmployeeMonthEndTasksService(MonthEndTaskRepository monthEndTaskRepository) {
        this.monthEndTaskRepository = monthEndTaskRepository;
    }

    @Override
    public List<MonthEndTask> complete(YearMonth month, UserId actorId) {
        List<MonthEndTask> completedTasks = monthEndTaskRepository.findOpenSubjectTasks(actorId, month)
                .stream()
                .filter(task -> task.type() == MonthEndTaskType.EMPLOYEE_TIME_CHECK)
                .filter(task -> task.isOpen() && task.canBeCompletedBy(actorId))
                .map(task -> task.complete(actorId))
                .toList();

        monthEndTaskRepository.saveAll(completedTasks);

        Log.infof("Completed %d time-check tasks for employee %s in month %s",
                completedTasks.size(), actorId.value(), month);

        return completedTasks;
    }
}
