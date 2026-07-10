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

class CloseLeistungsnachweisTasksForProjectServiceTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 8);

    private MonthEndTaskRepository monthEndTaskRepository;
    private CloseLeistungsnachweisTasksForProjectService service;

    @BeforeEach
    void setUp() {
        monthEndTaskRepository = mock(MonthEndTaskRepository.class);
        service = new CloseLeistungsnachweisTasksForProjectService(monthEndTaskRepository);
    }

    @Test
    void closeOpenTasks_shouldCompleteAllOpenTasksBySystem() {
        ProjectId projectId = ProjectId.generate();
        MonthEndTask openTask = openLeistungsnachweisTask(projectId);

        when(monthEndTaskRepository.findOpenLeistungsnachweisTasks(MONTH, projectId)).thenReturn(List.of(openTask));

        service.closeOpenTasks(projectId, MONTH);

        verify(monthEndTaskRepository).saveAll(org.mockito.ArgumentMatchers.argThat(tasks -> {
            MonthEndTask saved = tasks.getFirst();
            return saved.status() == MonthEndTaskStatus.CLOSED
                    && saved.completedBy().equals(SystemActor.USER_ID)
                    && saved.type() == MonthEndTaskType.LEISTUNGSNACHWEIS;
        }));
    }

    @Test
    void closeOpenTasks_shouldDoNothing_whenNoOpenTasksExist() {
        ProjectId projectId = ProjectId.generate();

        when(monthEndTaskRepository.findOpenLeistungsnachweisTasks(MONTH, projectId)).thenReturn(List.of());

        service.closeOpenTasks(projectId, MONTH);

        verify(monthEndTaskRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    private MonthEndTask openLeistungsnachweisTask(ProjectId projectId) {
        UserId employeeId = UserId.of(UUID.randomUUID());
        return MonthEndTask.create(
                MonthEndTaskId.generate(),
                MONTH,
                MonthEndTaskType.LEISTUNGSNACHWEIS,
                projectId,
                employeeId,
                Set.of(UserId.of(UUID.randomUUID()))
        );
    }
}
