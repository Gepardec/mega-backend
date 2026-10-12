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

class CompleteEmployeeMonthEndTasksServiceTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 8);

    private MonthEndTaskRepository monthEndTaskRepository;
    private CompleteEmployeeMonthEndTasksService service;

    @BeforeEach
    void setUp() {
        monthEndTaskRepository = mock(MonthEndTaskRepository.class);
        service = new CompleteEmployeeMonthEndTasksService(monthEndTaskRepository);
    }

    @Test
    void complete_shouldCompleteTasksAcrossAllProjects() {
        UserId employeeId = UserId.generate();
        MonthEndTask taskInProjectA = openTimeCheckTask(ProjectId.generate(), employeeId);
        MonthEndTask taskInProjectB = openTimeCheckTask(ProjectId.generate(), employeeId);

        when(monthEndTaskRepository.findOpenSubjectTasks(employeeId, MONTH))
                .thenReturn(List.of(taskInProjectA, taskInProjectB));

        List<MonthEndTask> completedTasks = service.complete(MONTH, employeeId);

        assertThat(completedTasks)
                .extracting(MonthEndTask::id)
                .containsExactlyInAnyOrder(taskInProjectA.id(), taskInProjectB.id());
        assertThat(completedTasks).allSatisfy(task -> {
            assertThat(task.status()).isEqualTo(MonthEndTaskStatus.DONE);
            assertThat(task.completedBy()).isEqualTo(employeeId);
        });
        verify(monthEndTaskRepository).saveAll(completedTasks);
    }

    @Test
    void complete_shouldSkipAlreadyDoneTasks() {
        UserId employeeId = UserId.generate();
        ProjectId projectId = ProjectId.generate();
        MonthEndTask openTask = openTimeCheckTask(projectId, employeeId);
        MonthEndTask alreadyDoneTask = openTimeCheckTask(projectId, employeeId).complete(employeeId);

        when(monthEndTaskRepository.findOpenSubjectTasks(employeeId, MONTH))
                .thenReturn(List.of(openTask, alreadyDoneTask));

        List<MonthEndTask> completedTasks = service.complete(MONTH, employeeId);

        assertThat(completedTasks)
                .extracting(MonthEndTask::id)
                .containsExactly(openTask.id());
        verify(monthEndTaskRepository).saveAll(completedTasks);
    }

    @Test
    void complete_shouldSaveNothing_whenNoOpenTasksExist() {
        UserId employeeId = UserId.generate();
        when(monthEndTaskRepository.findOpenSubjectTasks(employeeId, MONTH))
                .thenReturn(List.of());

        List<MonthEndTask> completedTasks = service.complete(MONTH, employeeId);

        assertThat(completedTasks).isEmpty();
        verify(monthEndTaskRepository).saveAll(List.of());
    }

    @Test
    void complete_shouldCompleteOnlyTimeCheck_whenLeadIsAlsoEmployeeOnOwnProject() {
        UserId leadId = UserId.generate();
        ProjectId projectId = ProjectId.generate();
        MonthEndTask timeCheck = openTimeCheckTask(projectId, leadId);
        MonthEndTask leadReview = openLeadTask(MonthEndTaskType.PROJECT_LEAD_REVIEW, projectId, leadId);
        MonthEndTask leistungsnachweis = openLeadTask(MonthEndTaskType.LEISTUNGSNACHWEIS, projectId, leadId);

        when(monthEndTaskRepository.findOpenSubjectTasks(leadId, MONTH))
                .thenReturn(List.of(timeCheck, leadReview, leistungsnachweis));

        List<MonthEndTask> completedTasks = service.complete(MONTH, leadId);

        assertThat(completedTasks)
                .extracting(MonthEndTask::id)
                .containsExactly(timeCheck.id());
        assertThat(leadReview.status()).isEqualTo(MonthEndTaskStatus.OPEN);
        assertThat(leistungsnachweis.status()).isEqualTo(MonthEndTaskStatus.OPEN);
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

    private MonthEndTask openLeadTask(MonthEndTaskType type, ProjectId projectId, UserId leadId) {
        return MonthEndTask.create(
                MonthEndTaskId.generate(),
                MONTH,
                type,
                projectId,
                leadId,
                Set.of(leadId)
        );
    }
}
