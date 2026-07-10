package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.ReopenLeistungsnachweisTasksForProjectUseCase;
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
public class ReopenLeistungsnachweisTasksForProjectService implements ReopenLeistungsnachweisTasksForProjectUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;

    @Inject
    public ReopenLeistungsnachweisTasksForProjectService(MonthEndTaskRepository monthEndTaskRepository) {
        this.monthEndTaskRepository = monthEndTaskRepository;
    }

    @Override
    public void reopenClosedTasks(ProjectId projectId, YearMonth month) {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(month, "month must not be null");

        List<MonthEndTask> closedTasks = monthEndTaskRepository.findClosedLeistungsnachweisTasks(month, projectId);
        if (closedTasks.isEmpty()) {
            return;
        }

        List<MonthEndTask> reopenedTasks = closedTasks.stream()
                .map(MonthEndTask::reopen)
                .toList();
        monthEndTaskRepository.saveAll(reopenedTasks);
    }
}
