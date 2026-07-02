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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProjectLeadPayrollMonthServiceTest {

    private final UserId leadId = UserId.of(Instancio.create(UUID.class));
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));

    @Mock
    private MonthEndTaskRepository monthEndTaskRepository;

    @InjectMocks
    private GetProjectLeadPayrollMonthService service;

    @Test
    void getPayrollMonth_shouldReturnCurrentMonth_whenTasksExistForCurrentMonth() {
        YearMonth currentMonth = YearMonth.now();
        when(monthEndTaskRepository.findLeadProjectTasks(leadId, currentMonth)).thenReturn(List.of(openLeadTask(currentMonth)));

        YearMonth payrollMonth = service.getPayrollMonth(leadId);

        assertThat(payrollMonth).isEqualTo(currentMonth);
        verify(monthEndTaskRepository).findLeadProjectTasks(leadId, currentMonth);
        verifyNoMoreInteractions(monthEndTaskRepository);
    }

    @Test
    void getPayrollMonth_shouldReturnPreviousMonth_whenNoTasksExistForCurrentMonth() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);
        when(monthEndTaskRepository.findLeadProjectTasks(leadId, currentMonth)).thenReturn(List.of());

        YearMonth payrollMonth = service.getPayrollMonth(leadId);

        assertThat(payrollMonth).isEqualTo(previousMonth);
        verify(monthEndTaskRepository).findLeadProjectTasks(leadId, currentMonth);
        verifyNoMoreInteractions(monthEndTaskRepository);
    }

    private MonthEndTask openLeadTask(YearMonth month) {
        return MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.ABRECHNUNG,
                projectId,
                null,
                Set.of(leadId)
        );
    }
}
