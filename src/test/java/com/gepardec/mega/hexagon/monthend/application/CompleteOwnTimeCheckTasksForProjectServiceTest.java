package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompleteOwnTimeCheckTasksForProjectServiceTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 8);

    private MonthEndTaskRepository monthEndTaskRepository;
    private CompleteOwnTimeCheckTasksForProjectService service;

    @BeforeEach
    void setUp() {
        monthEndTaskRepository = mock(MonthEndTaskRepository.class);
        service = new CompleteOwnTimeCheckTasksForProjectService(monthEndTaskRepository);
    }

    @Test
    void completeOwnTimeCheckTasks_shouldCompleteAllOpenTasks_forGivenProject() {
        ProjectId projectId = ProjectId.generate();
        UserId employeeId = UserId.generate();
        MonthEndTask openTask = openTimeCheckTask(projectId, employeeId);
        MonthEndTask alreadyDoneTask = openTimeCheckTask(projectId, employeeId).complete(employeeId);

        when(monthEndTaskRepository.findOpenEmployeeTimeCheckTasks(employeeId, MONTH, projectId))
                .thenReturn(List.of(openTask, alreadyDoneTask));

        List<MonthEndTask> completedTasks = service.completeOwnTimeCheckTasks(employeeId, MONTH, projectId);

        assertThat(completedTasks).hasSize(1);
        assertThat(completedTasks.getFirst().status()).isEqualTo(MonthEndTaskStatus.DONE);
        assertThat(completedTasks.getFirst().completedBy()).isEqualTo(employeeId);
        verify(monthEndTaskRepository).saveAll(completedTasks);
    }

    @Test
    void completeOwnTimeCheckTasks_shouldCompleteTasksAcrossAllProjects_whenProjectIdIsNull() {
        UserId employeeId = UserId.generate();
        MonthEndTask taskInProjectA = openTimeCheckTask(ProjectId.generate(), employeeId);
        MonthEndTask taskInProjectB = openTimeCheckTask(ProjectId.generate(), employeeId);

        when(monthEndTaskRepository.findOpenEmployeeTimeCheckTasks(employeeId, MONTH, null))
                .thenReturn(List.of(taskInProjectA, taskInProjectB));

        List<MonthEndTask> completedTasks = service.completeOwnTimeCheckTasks(employeeId, MONTH, null);

        assertThat(completedTasks).hasSize(2);
        assertThat(completedTasks).allSatisfy(task -> {
            assertThat(task.status()).isEqualTo(MonthEndTaskStatus.DONE);
            assertThat(task.completedBy()).isEqualTo(employeeId);
        });
        verify(monthEndTaskRepository).saveAll(completedTasks);
    }

    @Test
    void completeOwnTimeCheckTasks_shouldSaveNothing_whenNoOpenTasksExist() {
        UserId employeeId = UserId.generate();
        when(monthEndTaskRepository.findOpenEmployeeTimeCheckTasks(employeeId, MONTH, null))
                .thenReturn(List.of());

        List<MonthEndTask> completedTasks = service.completeOwnTimeCheckTasks(employeeId, MONTH, null);

        assertThat(completedTasks).isEmpty();
        verify(monthEndTaskRepository).saveAll(completedTasks);
    }

    private MonthEndTask openTimeCheckTask(ProjectId projectId, UserId employeeId) {
        return MonthEndTask.create(
                MonthEndTaskId.generate(),
                MONTH,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                employeeId,
                Set.of(employeeId)
        );
    }
}
