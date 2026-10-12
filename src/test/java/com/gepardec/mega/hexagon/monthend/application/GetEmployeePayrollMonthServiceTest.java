package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEmployeePayrollMonthServiceTest {

    private final UserId actorId = UserId.of(Instancio.create(UUID.class));
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));

    @Mock
    private MonthEndTaskRepository monthEndTaskRepository;

    @InjectMocks
    private GetEmployeePayrollMonthService service;

    @Test
    void getPayrollMonth_shouldReturnCurrentMonth_whenOpenTasksExistForCurrentMonthAndPreviousMonth() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);
        MonthEndTask currentMonthTask = openTask(currentMonth);
        MonthEndTask previousMonthTask = openTask(previousMonth);
        when(monthEndTaskRepository.findOpenSubjectTasks(actorId, currentMonth)).thenReturn(List.of(currentMonthTask));
        lenient().when(monthEndTaskRepository.findOpenSubjectTasks(actorId, previousMonth)).thenReturn(List.of(previousMonthTask));

        YearMonth payrollMonth = service.getPayrollMonth(actorId);

        assertThat(payrollMonth).isEqualTo(currentMonth);
        verify(monthEndTaskRepository).findOpenSubjectTasks(actorId, currentMonth);
        verifyNoMoreInteractions(monthEndTaskRepository);
    }

    @Test
    void getPayrollMonth_shouldReturnPreviousMonth_whenOpenTasksExistForPreviousMonthAndCurrentMonthIsEmpty() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);
        MonthEndTask openTask = openTask(previousMonth);
        when(monthEndTaskRepository.findOpenSubjectTasks(actorId, currentMonth)).thenReturn(List.of());
        when(monthEndTaskRepository.findOpenSubjectTasks(actorId, previousMonth)).thenReturn(List.of(openTask));

        YearMonth payrollMonth = service.getPayrollMonth(actorId);

        assertThat(payrollMonth).isEqualTo(previousMonth);
        verify(monthEndTaskRepository).findOpenSubjectTasks(actorId, currentMonth);
        verify(monthEndTaskRepository).findOpenSubjectTasks(actorId, previousMonth);
        verifyNoMoreInteractions(monthEndTaskRepository);
    }

    @Test
    void getPayrollMonth_shouldReturnCurrentMonth_whenNoOpenTasksExistForPreviousMonth() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);
        when(monthEndTaskRepository.findOpenSubjectTasks(actorId, currentMonth)).thenReturn(List.of());
        when(monthEndTaskRepository.findOpenSubjectTasks(actorId, previousMonth)).thenReturn(List.of());

        YearMonth payrollMonth = service.getPayrollMonth(actorId);

        assertThat(payrollMonth).isEqualTo(currentMonth);
        verify(monthEndTaskRepository).findOpenSubjectTasks(actorId, currentMonth);
        verify(monthEndTaskRepository).findOpenSubjectTasks(actorId, previousMonth);
        verifyNoMoreInteractions(monthEndTaskRepository);
    }

    private MonthEndTask openTask(YearMonth month) {
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
