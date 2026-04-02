package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateMonthEndClarificationServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId employeeId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadA = UserId.of(Instancio.create(UUID.class));
    private final UserId leadB = UserId.of(Instancio.create(UUID.class));
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-31T08:00:00Z"), ZoneOffset.UTC);

    private MonthEndClarificationRepository clarificationRepository;
    private MonthEndTaskRepository monthEndTaskRepository;
    private CreateMonthEndClarificationService service;

    @BeforeEach
    void setUp() {
        clarificationRepository = mock(MonthEndClarificationRepository.class);
        monthEndTaskRepository = mock(MonthEndTaskRepository.class);
        service = new CreateMonthEndClarificationService(clarificationRepository, monthEndTaskRepository, clock);
    }

    @Test
    void create_shouldPersistClarification_whenEmployeeCreatesInValidContext() {
        when(monthEndTaskRepository.findProjectLeadReviewTask(month, projectId, employeeId))
                .thenReturn(Optional.of(openLeadReviewTask()));

        MonthEndClarification result = service.create(
                month,
                projectId,
                employeeId,
                employeeId,
                MonthEndClarificationSide.EMPLOYEE,
                "Please check this project entry."
        );

        assertThat(result.creatorSide()).isEqualTo(MonthEndClarificationSide.EMPLOYEE);
        assertThat(result.eligibleProjectLeadIds()).containsExactlyInAnyOrder(leadA, leadB);
        assertThat(result.createdAt()).isEqualTo(clock.instant());
        verify(clarificationRepository).save(any(MonthEndClarification.class));
    }

    @Test
    void create_shouldAllowLeadSideForDualRoleUser_whenRequestedExplicitly() {
        MonthEndTask contextTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                employeeId,
                Set.of(employeeId, leadB)
        );
        when(monthEndTaskRepository.findProjectLeadReviewTask(month, projectId, employeeId))
                .thenReturn(Optional.of(contextTask));

        MonthEndClarification result = service.create(
                month,
                projectId,
                employeeId,
                employeeId,
                MonthEndClarificationSide.PROJECT_LEAD,
                "Please update this from the lead perspective."
        );

        assertThat(result.creatorSide()).isEqualTo(MonthEndClarificationSide.PROJECT_LEAD);
        verify(clarificationRepository).save(any(MonthEndClarification.class));
    }

    @Test
    void create_shouldThrow_whenLeadReviewContextIsMissing() {
        when(monthEndTaskRepository.findProjectLeadReviewTask(month, projectId, employeeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(
                month,
                projectId,
                employeeId,
                employeeId,
                MonthEndClarificationSide.EMPLOYEE,
                "Please check this."
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("context not found");
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
