package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.SystemActor;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReopenLeistungsnachweisTasksForProjectServiceTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 8);

    private MonthEndTaskRepository monthEndTaskRepository;
    private ReopenLeistungsnachweisTasksForProjectService service;

    @BeforeEach
    void setUp() {
        monthEndTaskRepository = mock(MonthEndTaskRepository.class);
        service = new ReopenLeistungsnachweisTasksForProjectService(monthEndTaskRepository);
    }

    @Test
    void reopenClosedTasks_shouldReopenAllClosedTasks() {
        ProjectId projectId = ProjectId.generate();
        MonthEndTask closedTask = closedLeistungsnachweisTask(projectId);

        when(monthEndTaskRepository.findClosedLeistungsnachweisTasks(MONTH, projectId)).thenReturn(List.of(closedTask));

        service.reopenClosedTasks(projectId, MONTH);

        verify(monthEndTaskRepository).saveAll(org.mockito.ArgumentMatchers.argThat(tasks -> {
            MonthEndTask saved = tasks.getFirst();
            return saved.status() == MonthEndTaskStatus.OPEN
                    && saved.completedBy() == null
                    && saved.type() == MonthEndTaskType.LEISTUNGSNACHWEIS;
        }));
    }

    @Test
    void reopenClosedTasks_shouldDoNothing_whenNoClosedTasksExist() {
        ProjectId projectId = ProjectId.generate();

        when(monthEndTaskRepository.findClosedLeistungsnachweisTasks(MONTH, projectId)).thenReturn(List.of());

        service.reopenClosedTasks(projectId, MONTH);

        verify(monthEndTaskRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    private MonthEndTask closedLeistungsnachweisTask(ProjectId projectId) {
        UserId employeeId = UserId.of(UUID.randomUUID());
        return MonthEndTask.create(
                MonthEndTaskId.generate(),
                MONTH,
                MonthEndTaskType.LEISTUNGSNACHWEIS,
                projectId,
                employeeId,
                Set.of(UserId.of(UUID.randomUUID()))
        ).closeBySystem();
    }
}
