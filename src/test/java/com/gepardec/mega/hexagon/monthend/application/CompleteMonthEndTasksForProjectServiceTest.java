package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndActorNotAuthorizedException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndProjectContextNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.model.*;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class CompleteMonthEndTasksForProjectServiceTest {
    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId employeeId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId leadA = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId leadB = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));

    private MonthEndTaskRepository monthEndTaskRepository;
    private MonthEndProjectContextService monthEndProjectContextService;
    private CompleteMonthEndTasksForProjectService service;

    @BeforeEach
    void setUp() {
        monthEndTaskRepository = mock(MonthEndTaskRepository.class);
        monthEndProjectContextService = mock(MonthEndProjectContextService.class);
        service = new CompleteMonthEndTasksForProjectService(monthEndTaskRepository, monthEndProjectContextService);
    }

    @Test
    void complete_shouldCompleteAllOpenEligibleTasks_whenActorEligible() {
        MonthEndTask task1 = openLeadReviewTask();
        MonthEndTask task2 = openLeadReviewTask();
        MonthEndTask task3 = openLeadReviewTask().complete(leadA);

        when(monthEndProjectContextService.resolve(month, projectId)).thenReturn(new MonthEndProjectContext(month, new MonthEndProjectSnapshot(projectId, 77, "Project", true, true, Set.of(leadA)), Set.of(leadA)));
        when(monthEndTaskRepository.findByProjectMonthAndType(month,projectId,task1.type())).thenReturn(List.of(task1, task2, task3));

        List<MonthEndTask> completedTasks = service.complete(month, projectId, task1.type(), leadA);

        assertThat(completedTasks)
                .extracting(MonthEndTask::status)
                .containsOnly(MonthEndTaskStatus.DONE);

        assertThat(completedTasks)
                .extracting(MonthEndTask::completedBy)
                .contains(leadA);

        assertThat(completedTasks).doesNotContain(task3);
        verify(monthEndTaskRepository).saveAll(completedTasks);
    }


    @Test
    void complete_shouldReturnEmptyResults_whenReRun() {
        MonthEndTask task1 = openLeadReviewTask();
        MonthEndTask task2 = openLeadReviewTask();
        MonthEndTask task3 = openLeadReviewTask();

        when(monthEndProjectContextService.resolve(month, projectId)).thenReturn(new MonthEndProjectContext(month, new MonthEndProjectSnapshot(projectId, 77, "Project", true, true, Set.of(leadA)), Set.of(leadA)));
        when(monthEndTaskRepository.findByProjectMonthAndType(month,projectId,task1.type())).thenReturn(List.of(task1.complete(leadA), task2.complete(leadA), task3.complete(leadA)));

        List<MonthEndTask> completedTasks = service.complete(month, projectId, task1.type(), leadA);

        assertThat(completedTasks).hasSize(0);
        verify(monthEndTaskRepository).saveAll(completedTasks);

    }

    @Test
    void complete_shouldRejectActor_whenActorIsNotEligible() {
        when(monthEndProjectContextService.resolve(month, projectId)).thenReturn(new MonthEndProjectContext(month, new MonthEndProjectSnapshot(projectId, 77, "Project", true, true, Set.of(leadA)), Set.of(leadA)));

        assertThatThrownBy(() -> service.complete(month, projectId, MonthEndTaskType.PROJECT_LEAD_REVIEW, leadB))
                .isInstanceOf(MonthEndActorNotAuthorizedException.class)
                .hasMessageContaining("actor not authorized: ");

        verifyNoInteractions(monthEndTaskRepository);
    }

    @Test
    void complete_shouldRejectProject_whenProjectIsUnknownOrInactive() {
        ProjectId projectIdNonExistent = ProjectId.of(UUID.fromString(Instancio.gen().text().uuid().get()));

        when(monthEndProjectContextService.resolve(month, projectIdNonExistent)).
                thenThrow(new MonthEndProjectContextNotFoundException("month-end project context not found for project %s in %s".formatted(projectIdNonExistent.value(), month)));

        assertThatThrownBy(() -> service.complete(month, projectIdNonExistent, MonthEndTaskType.PROJECT_LEAD_REVIEW, leadB))
                .isInstanceOf(MonthEndProjectContextNotFoundException.class)
                .hasMessageContaining("month-end project context not found for project");

        verify(monthEndTaskRepository, never()).findByProjectMonthAndType(any(), any(),  any());
    }

    private MonthEndTask openLeadReviewTask() {
        return MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                employeeId,
                Set.of(leadA, leadB)
        );
    }
}
