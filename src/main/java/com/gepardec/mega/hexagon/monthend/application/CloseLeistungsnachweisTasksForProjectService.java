package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CloseLeistungsnachweisTasksForProjectUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
@Transactional
public class CloseLeistungsnachweisTasksForProjectService implements CloseLeistungsnachweisTasksForProjectUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;

    @Inject
    public CloseLeistungsnachweisTasksForProjectService(MonthEndTaskRepository monthEndTaskRepository) {
        this.monthEndTaskRepository = monthEndTaskRepository;
    }

    @Override
    public void closeOpenTasks(ProjectId projectId, YearMonth month) {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(month, "month must not be null");

        List<MonthEndTask> openTasks = monthEndTaskRepository.findOpenLeistungsnachweisTasks(month, projectId);
        if (openTasks.isEmpty()) {
            return;
        }

        List<MonthEndTask> completedTasks = openTasks.stream()
                .map(MonthEndTask::completeBySystem)
                .toList();
        monthEndTaskRepository.saveAll(completedTasks);
    }
}
