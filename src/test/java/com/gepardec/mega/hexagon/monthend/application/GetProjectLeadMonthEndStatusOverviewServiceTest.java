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
class GetProjectLeadMonthEndStatusOverviewServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId leadId = UserId.of(Instancio.create(UUID.class));
    private final UserId subjectEmployeeId = UserId.of(Instancio.create(UUID.class));

    @Mock
    private MonthEndTaskRepository monthEndTaskRepository;

    @Mock
    private MonthEndClarificationRepository monthEndClarificationRepository;

    private GetProjectLeadMonthEndStatusOverviewService service;

    @BeforeEach
    void setUp() {
        service = new GetProjectLeadMonthEndStatusOverviewService(
                monthEndTaskRepository,
                monthEndClarificationRepository
        );
    }

    @Test
    void getOverview_shouldReturnTasksAndClarificationsDirectly() {
        MonthEndTask task = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                subjectEmployeeId,
                Set.of(leadId)
        );
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                subjectEmployeeId,
                subjectEmployeeId,
                Set.of(leadId),
                "Please verify the employee clarification.",
                Instant.parse("2026-03-31T08:00:00Z")
        );

        when(monthEndTaskRepository.findLeadProjectTasks(leadId, month)).thenReturn(List.of(task));
        when(monthEndClarificationRepository.findAllProjectLeadClarifications(leadId, month))
                .thenReturn(List.of(clarification));

        MonthEndStatusOverview overview = service.getOverview(leadId, month);

        assertThat(overview.actorId()).isEqualTo(leadId);
        assertThat(overview.month()).isEqualTo(month);
        assertThat(overview.tasks()).containsExactly(task);
        assertThat(overview.clarifications()).containsExactly(clarification);
        verify(monthEndTaskRepository).findLeadProjectTasks(leadId, month);
        verify(monthEndClarificationRepository).findAllProjectLeadClarifications(leadId, month);
    }

    @Test
    void getOverview_shouldReturnEmptyOverview_whenNoTasksOrClarifications() {
        when(monthEndTaskRepository.findLeadProjectTasks(leadId, month)).thenReturn(List.of());
        when(monthEndClarificationRepository.findAllProjectLeadClarifications(leadId, month)).thenReturn(List.of());

        MonthEndStatusOverview overview = service.getOverview(leadId, month);

        assertThat(overview.tasks()).isEmpty();
        assertThat(overview.clarifications()).isEmpty();
    }
}
