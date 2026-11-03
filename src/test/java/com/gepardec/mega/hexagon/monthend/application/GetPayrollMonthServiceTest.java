package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndScope;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
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

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPayrollMonthServiceTest {

    private static final YearMonth CURRENT_MONTH = YearMonth.of(2026, 3);
    private static final YearMonth PREVIOUS_MONTH = YearMonth.of(2026, 2);

    private final UserId actorId = UserId.of(Instancio.create(UUID.class));
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));

    private final Clock clock = Clock.fixed(Instant.parse("2026-03-15T08:00:00Z"), ZoneOffset.UTC);

    @Mock
    private MonthEndTaskRepository monthEndTaskRepository;

    private GetPayrollMonthService service;

    @BeforeEach
    void setUp() {
        service = new GetPayrollMonthService(monthEndTaskRepository, clock);
    }

    @Test
    void getPayrollMonth_shouldReturnCurrentMonth_inEmployeeCapacity_whenOpenTasksExistForCurrentAndPreviousMonth() {
        when(monthEndTaskRepository.findOpenSubjectTasks(actorId, CURRENT_MONTH)).thenReturn(List.of(openTimeCheck(CURRENT_MONTH)));
        lenient().when(monthEndTaskRepository.findOpenSubjectTasks(actorId, PREVIOUS_MONTH)).thenReturn(List.of(openTimeCheck(PREVIOUS_MONTH)));

        YearMonth payrollMonth = service.getPayrollMonth(actorId, Capacity.EMPLOYEE);

        assertThat(payrollMonth).isEqualTo(CURRENT_MONTH);
        verify(monthEndTaskRepository).findOpenSubjectTasks(actorId, CURRENT_MONTH);
        verifyNoMoreInteractions(monthEndTaskRepository);
    }

    @Test
    void getPayrollMonth_shouldReturnPreviousMonth_inEmployeeCapacity_whenOnlyPreviousMonthHasOpenTasks() {
        when(monthEndTaskRepository.findOpenSubjectTasks(actorId, CURRENT_MONTH)).thenReturn(List.of());
        when(monthEndTaskRepository.findOpenSubjectTasks(actorId, PREVIOUS_MONTH)).thenReturn(List.of(openTimeCheck(PREVIOUS_MONTH)));

        YearMonth payrollMonth = service.getPayrollMonth(actorId, Capacity.EMPLOYEE);

        assertThat(payrollMonth).isEqualTo(PREVIOUS_MONTH);
        verify(monthEndTaskRepository).findOpenSubjectTasks(actorId, CURRENT_MONTH);
        verify(monthEndTaskRepository).findOpenSubjectTasks(actorId, PREVIOUS_MONTH);
        verifyNoMoreInteractions(monthEndTaskRepository);
    }

    @Test
    void getPayrollMonth_shouldReturnCurrentMonth_inEmployeeCapacity_whenPreviousMonthHasNoOpenTasks() {
        when(monthEndTaskRepository.findOpenSubjectTasks(actorId, CURRENT_MONTH)).thenReturn(List.of());
        when(monthEndTaskRepository.findOpenSubjectTasks(actorId, PREVIOUS_MONTH)).thenReturn(List.of());

        YearMonth payrollMonth = service.getPayrollMonth(actorId, Capacity.EMPLOYEE);

        assertThat(payrollMonth).isEqualTo(CURRENT_MONTH);
        verify(monthEndTaskRepository).findOpenSubjectTasks(actorId, CURRENT_MONTH);
        verify(monthEndTaskRepository).findOpenSubjectTasks(actorId, PREVIOUS_MONTH);
        verifyNoMoreInteractions(monthEndTaskRepository);
    }

    @Test
    void getPayrollMonth_shouldReturnCurrentMonth_inEmployeeCapacity_whenNoTasksWereGeneratedYet() {
        when(monthEndTaskRepository.findOpenSubjectTasks(actorId, CURRENT_MONTH)).thenReturn(List.of());
        when(monthEndTaskRepository.findOpenSubjectTasks(actorId, PREVIOUS_MONTH)).thenReturn(List.of());

        assertThat(service.getPayrollMonth(actorId, Capacity.EMPLOYEE)).isEqualTo(CURRENT_MONTH);
    }

    @Test
    void getPayrollMonth_shouldReturnCurrentMonth_inControllingCapacity_whenTasksExistForCurrentMonth() {
        MonthEndScope scope = new MonthEndScope.LedProjects(actorId);
        when(monthEndTaskRepository.existsTasks(CURRENT_MONTH, scope)).thenReturn(true);

        YearMonth payrollMonth = service.getPayrollMonth(actorId, Capacity.CONTROLLING);

        assertThat(payrollMonth).isEqualTo(CURRENT_MONTH);
        verify(monthEndTaskRepository).existsTasks(CURRENT_MONTH, scope);
        verifyNoMoreInteractions(monthEndTaskRepository);
    }

    @Test
    void getPayrollMonth_shouldReturnPreviousMonth_inControllingCapacity_whenNoTasksExistForCurrentMonth() {
        MonthEndScope scope = new MonthEndScope.LedProjects(actorId);
        when(monthEndTaskRepository.existsTasks(CURRENT_MONTH, scope)).thenReturn(false);

        YearMonth payrollMonth = service.getPayrollMonth(actorId, Capacity.CONTROLLING);

        assertThat(payrollMonth).isEqualTo(PREVIOUS_MONTH);
        verify(monthEndTaskRepository).existsTasks(CURRENT_MONTH, scope);
        verifyNoMoreInteractions(monthEndTaskRepository);
    }

    private MonthEndTask openTimeCheck(YearMonth month) {
        return MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                actorId,
                Set.of(actorId)
        );
    }
}
