package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndTaskNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompleteMonthEndTaskServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId employeeId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId leadA = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId leadB = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));

    private MonthEndTaskRepository monthEndTaskRepository;
    private CompleteMonthEndTaskService service;

    @BeforeEach
    void setUp() {
        monthEndTaskRepository = mock(MonthEndTaskRepository.class);
        service = new CompleteMonthEndTaskService(monthEndTaskRepository);
    }

    @Test
    void complete_shouldPersistCompletedTask_whenActorEligible() {
        MonthEndTask task = openLeadReviewTask();
        when(monthEndTaskRepository.findById(task.id())).thenReturn(Optional.of(task));

        MonthEndTask result = service.complete(task.id(), leadA);

        assertThat(result.status()).isEqualTo(MonthEndTaskStatus.DONE);
        assertThat(result.completedBy()).isEqualTo(leadA);
        verify(monthEndTaskRepository).save(result);
    }

    @Test
    void complete_shouldNotPersistAgain_whenTaskAlreadyDone() {
        MonthEndTask completedTask = openLeadReviewTask().complete(leadA);
        when(monthEndTaskRepository.findById(completedTask.id())).thenReturn(Optional.of(completedTask));

        MonthEndTask result = service.complete(completedTask.id(), leadB);

        assertThat(result.completedBy()).isEqualTo(leadA);
        verify(monthEndTaskRepository, never()).save(completedTask);
    }

    @Test
    void complete_shouldThrow_whenTaskIsMissing() {
        MonthEndTaskId taskId = MonthEndTaskId.generate();
        when(monthEndTaskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.complete(taskId, leadA))
                .isInstanceOf(MonthEndTaskNotFoundException.class)
                .hasMessageContaining("not found");
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
