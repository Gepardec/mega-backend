package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndScope;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.Capacity;
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
class GetMonthEndStatusOverviewServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId actorId = UserId.of(Instancio.create(UUID.class));
    private final UserId otherId = UserId.of(Instancio.create(UUID.class));

    @Mock
    private MonthEndTaskRepository monthEndTaskRepository;

    @Mock
    private MonthEndClarificationRepository monthEndClarificationRepository;

    private GetMonthEndStatusOverviewService service;

    @BeforeEach
    void setUp() {
        service = new GetMonthEndStatusOverviewService(monthEndTaskRepository, monthEndClarificationRepository);
    }

    @Test
    void getOverview_shouldQuerySubjectEmployeeScope_inEmployeeCapacity() {
        MonthEndTask task = timeCheck();
        MonthEndClarification clarification = clarification();
        MonthEndScope scope = new MonthEndScope.SubjectEmployee(actorId);
        when(monthEndTaskRepository.findTasks(month, scope)).thenReturn(List.of(task));
        when(monthEndClarificationRepository.findClarifications(month, scope)).thenReturn(List.of(clarification));

        MonthEndStatusOverview overview = service.getOverview(actorId, month, Capacity.EMPLOYEE);

        assertThat(overview.actorId()).isEqualTo(actorId);
        assertThat(overview.month()).isEqualTo(month);
        assertThat(overview.tasks()).containsExactly(task);
        assertThat(overview.clarifications()).containsExactly(clarification);
        verify(monthEndTaskRepository).findTasks(month, scope);
        verify(monthEndClarificationRepository).findClarifications(month, scope);
    }

    @Test
    void getOverview_shouldQueryLedProjectsScope_inControllingCapacity() {
        MonthEndTask task = timeCheck();
        MonthEndClarification clarification = clarification();
        MonthEndScope scope = new MonthEndScope.LedProjects(actorId);
        when(monthEndTaskRepository.findTasks(month, scope)).thenReturn(List.of(task));
        when(monthEndClarificationRepository.findClarifications(month, scope)).thenReturn(List.of(clarification));

        MonthEndStatusOverview overview = service.getOverview(actorId, month, Capacity.CONTROLLING);

        assertThat(overview.tasks()).containsExactly(task);
        assertThat(overview.clarifications()).containsExactly(clarification);
        verify(monthEndTaskRepository).findTasks(month, scope);
        verify(monthEndClarificationRepository).findClarifications(month, scope);
    }

    @Test
    void getOverview_shouldReturnEmptyOverview_whenNoTasksOrClarifications() {
        MonthEndScope scope = new MonthEndScope.SubjectEmployee(actorId);
        when(monthEndTaskRepository.findTasks(month, scope)).thenReturn(List.of());
        when(monthEndClarificationRepository.findClarifications(month, scope)).thenReturn(List.of());

        MonthEndStatusOverview overview = service.getOverview(actorId, month, Capacity.EMPLOYEE);

        assertThat(overview.tasks()).isEmpty();
        assertThat(overview.clarifications()).isEmpty();
    }

    private MonthEndTask timeCheck() {
        return MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                actorId,
                Set.of(actorId)
        );
    }

    private MonthEndClarification clarification() {
        return MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                actorId,
                otherId,
                Set.of(otherId),
                "Please add the remaining note.",
                Instant.parse("2026-03-31T08:00:00Z")
        );
    }
}
