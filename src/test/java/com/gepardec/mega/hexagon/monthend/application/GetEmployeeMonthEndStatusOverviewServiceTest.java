package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEmployeeMonthEndStatusOverviewServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId actorId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadId = UserId.of(Instancio.create(UUID.class));

    @Mock
    private MonthEndTaskRepository monthEndTaskRepository;

    @Mock
    private MonthEndClarificationRepository monthEndClarificationRepository;

    private GetEmployeeMonthEndStatusOverviewService service;

    @BeforeEach
    void setUp() {
        service = new GetEmployeeMonthEndStatusOverviewService(
                monthEndTaskRepository,
                monthEndClarificationRepository
        );
    }

    @Test
    void getOverview_shouldReturnTasksAndClarificationsDirectly() {
        MonthEndTask task = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                actorId,
                Set.of(actorId)
        );
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                actorId,
                leadId,
                Set.of(leadId),
                "Please add the remaining note.",
                Instant.parse("2026-03-31T08:00:00Z")
        );

        when(monthEndTaskRepository.findEmployeeVisibleTasks(actorId, month)).thenReturn(List.of(task));
        when(monthEndClarificationRepository.findAllEmployeeClarifications(actorId, month))
                .thenReturn(List.of(clarification));

        MonthEndStatusOverview overview = service.getOverview(actorId, month);

        assertThat(overview.actorId()).isEqualTo(actorId);
        assertThat(overview.month()).isEqualTo(month);
        assertThat(overview.tasks()).containsExactly(task);
        assertThat(overview.clarifications()).containsExactly(clarification);
        verify(monthEndTaskRepository).findEmployeeVisibleTasks(actorId, month);
        verify(monthEndClarificationRepository).findAllEmployeeClarifications(actorId, month);
    }

    @Test
    void getOverview_shouldReturnEmptyOverview_whenNoTasksOrClarifications() {
        when(monthEndTaskRepository.findEmployeeVisibleTasks(actorId, month)).thenReturn(List.of());
        when(monthEndClarificationRepository.findAllEmployeeClarifications(actorId, month)).thenReturn(List.of());

        MonthEndStatusOverview overview = service.getOverview(actorId, month);

        assertThat(overview.tasks()).isEmpty();
        assertThat(overview.clarifications()).isEmpty();
    }
}
