package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndErrorCode;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CompleteProjectLeadMonthEndTasksServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId employeeId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId leadA = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId leadB = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));

    private MonthEndTaskRepository monthEndTaskRepository;
    private CompleteProjectLeadMonthEndTasksService service;

    @BeforeEach
    void setUp() {
        monthEndTaskRepository = mock(MonthEndTaskRepository.class);
        service = new CompleteProjectLeadMonthEndTasksService(monthEndTaskRepository);
    }

    @Test
    void complete_shouldCompleteAllOpenEligibleTasks_whenActorLeadsProject() {
        MonthEndTask task1 = openLeadReviewTask();
        MonthEndTask task2 = openLeadReviewTask();
        MonthEndTask task3 = openLeadReviewTask().complete(leadA);

        when(monthEndTaskRepository.existsLeadTask(month, projectId, leadA)).thenReturn(true);
        when(monthEndTaskRepository.findOpenProjectTasksOfType(month, projectId, MonthEndTaskType.PROJECT_LEAD_REVIEW))
                .thenReturn(List.of(task1, task2, task3));

        List<MonthEndTask> completedTasks = service.complete(month, projectId, MonthEndTaskType.PROJECT_LEAD_REVIEW, leadA);

        assertThat(completedTasks)
                .extracting(MonthEndTask::id)
                .containsExactlyInAnyOrder(task1.id(), task2.id());
        assertThat(completedTasks)
                .extracting(MonthEndTask::status)
                .containsOnly(MonthEndTaskStatus.DONE);
        assertThat(completedTasks)
                .extracting(MonthEndTask::completedBy)
                .containsOnly(leadA);
        verify(monthEndTaskRepository).saveAll(completedTasks);
    }

    @Test
    void complete_shouldCompleteTasks_whenLeadWasRemovedFromProjectAfterGeneration() {
        // eligibility recorded on the tasks at generation time is the only source of truth
        MonthEndTask task = openLeadReviewTask(Set.of(leadA));

        when(monthEndTaskRepository.existsLeadTask(month, projectId, leadA)).thenReturn(true);
        when(monthEndTaskRepository.findOpenProjectTasksOfType(month, projectId, MonthEndTaskType.PROJECT_LEAD_REVIEW))
                .thenReturn(List.of(task));

        List<MonthEndTask> completedTasks = service.complete(month, projectId, MonthEndTaskType.PROJECT_LEAD_REVIEW, leadA);

        assertThat(completedTasks)
                .extracting(MonthEndTask::id)
                .containsExactly(task.id());
        verify(monthEndTaskRepository).saveAll(completedTasks);
    }

    @Test
    void complete_shouldReturnEmptyResults_whenReRun() {
        MonthEndTask task1 = openLeadReviewTask().complete(leadA);
        MonthEndTask task2 = openLeadReviewTask().complete(leadA);

        when(monthEndTaskRepository.existsLeadTask(month, projectId, leadA)).thenReturn(true);
        when(monthEndTaskRepository.findOpenProjectTasksOfType(month, projectId, MonthEndTaskType.PROJECT_LEAD_REVIEW))
                .thenReturn(List.of(task1, task2));

        List<MonthEndTask> completedTasks = service.complete(month, projectId, MonthEndTaskType.PROJECT_LEAD_REVIEW, leadA);

        assertThat(completedTasks).isEmpty();
        verify(monthEndTaskRepository).saveAll(List.of());
    }

    @Test
    void complete_shouldReturnEmptyResults_whenLeadHasNoTasksOfRequestedType() {
        when(monthEndTaskRepository.existsLeadTask(month, projectId, leadA)).thenReturn(true);
        when(monthEndTaskRepository.findOpenProjectTasksOfType(month, projectId, MonthEndTaskType.LEISTUNGSNACHWEIS))
                .thenReturn(List.of());

        List<MonthEndTask> completedTasks = service.complete(month, projectId, MonthEndTaskType.LEISTUNGSNACHWEIS, leadA);

        assertThat(completedTasks).isEmpty();
        verify(monthEndTaskRepository).saveAll(List.of());
    }

    @Test
    void complete_shouldSkipTask_whenActorIsNotInTasksEligibleActors() {
        MonthEndTask eligibleTask = openLeadReviewTask();
        MonthEndTask notEligibleTask = openLeadReviewTask(Set.of(leadB));

        when(monthEndTaskRepository.existsLeadTask(month, projectId, leadA)).thenReturn(true);
        when(monthEndTaskRepository.findOpenProjectTasksOfType(month, projectId, MonthEndTaskType.PROJECT_LEAD_REVIEW))
                .thenReturn(List.of(eligibleTask, notEligibleTask));

        List<MonthEndTask> completedTasks = service.complete(month, projectId, MonthEndTaskType.PROJECT_LEAD_REVIEW, leadA);

        assertThat(completedTasks)
                .extracting(MonthEndTask::id)
                .containsExactly(eligibleTask.id());
        assertThat(notEligibleTask.status()).isEqualTo(MonthEndTaskStatus.OPEN);
        verify(monthEndTaskRepository).saveAll(completedTasks);
    }

    @Test
    void complete_shouldRejectActor_whenActorDoesNotLeadProject() {
        when(monthEndTaskRepository.existsLeadTask(month, projectId, leadB)).thenReturn(false);

        assertThatThrownBy(() -> service.complete(month, projectId, MonthEndTaskType.PROJECT_LEAD_REVIEW, leadB))
                .isInstanceOfSatisfying(MonthEndException.class,
                        thrown -> assertThat(thrown.errorCode()).isEqualTo(MonthEndErrorCode.ACTOR_NOT_AUTHORIZED))
                .hasMessageContaining("actor not authorized: ");

        verify(monthEndTaskRepository, never()).findOpenProjectTasksOfType(any(), any(), any());
        verify(monthEndTaskRepository, never()).saveAll(any());
    }

    @Test
    void complete_shouldRejectActor_whenProjectIsUnknown() {
        ProjectId unknownProjectId = ProjectId.generate();
        when(monthEndTaskRepository.existsLeadTask(month, unknownProjectId, leadA)).thenReturn(false);

        assertThatThrownBy(() -> service.complete(month, unknownProjectId, MonthEndTaskType.PROJECT_LEAD_REVIEW, leadA))
                .isInstanceOfSatisfying(MonthEndException.class,
                        thrown -> assertThat(thrown.errorCode()).isEqualTo(MonthEndErrorCode.ACTOR_NOT_AUTHORIZED));

        verify(monthEndTaskRepository, never()).findOpenProjectTasksOfType(any(), any(), any());
        verify(monthEndTaskRepository, never()).saveAll(any());
    }

    @ParameterizedTest
    @EnumSource(value = MonthEndTaskType.class, names = {"EMPLOYEE_TIME_CHECK", "ABRECHNUNG"})
    void complete_shouldRejectType_whenTypeIsNotProjectLeadBulkCompletable(MonthEndTaskType type) {
        assertThatThrownBy(() -> service.complete(month, projectId, type, leadA))
                .isInstanceOfSatisfying(MonthEndException.class,
                        thrown -> assertThat(thrown.errorCode()).isEqualTo(MonthEndErrorCode.VALIDATION_FAILED))
                .hasMessageContaining(type.name());

        verifyNoInteractions(monthEndTaskRepository);
    }

    private MonthEndTask openLeadReviewTask() {
        return openLeadReviewTask(Set.of(leadA, leadB));
    }

    private MonthEndTask openLeadReviewTask(Set<UserId> eligibleActorIds) {
        return MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                employeeId,
                eligibleActorIds
        );
    }
}
