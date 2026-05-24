package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PayrollMonthCompletionAdapterTest {

    private MonthEndTaskRepository monthEndTaskRepository;
    private PayrollMonthCompletionAdapter adapter;

    @BeforeEach
    void setUp() {
        monthEndTaskRepository = mock(MonthEndTaskRepository.class);
        adapter = new PayrollMonthCompletionAdapter();
        adapter.monthEndTaskRepository = monthEndTaskRepository;
    }

    @Test
    void findUsersWithAllTasksCompleted_shouldIncludeOnlyEmployeesWhoseTasksAreAllDone() {
        YearMonth month = YearMonth.of(2026, 4);
        ProjectId projectId = ProjectId.generate();

        UserId allDoneEmployee = UserId.generate();
        UserId mixedEmployee = UserId.generate();
        UserId leadId = UserId.generate();

        MonthEndTask doneTaskA = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                allDoneEmployee,
                Set.of(allDoneEmployee)
        ).complete(allDoneEmployee);
        MonthEndTask doneTaskB = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.LEISTUNGSNACHWEIS,
                projectId,
                allDoneEmployee,
                Set.of(leadId)
        ).complete(leadId);

        MonthEndTask doneMixedTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                mixedEmployee,
                Set.of(mixedEmployee)
        ).complete(mixedEmployee);
        MonthEndTask openMixedTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.LEISTUNGSNACHWEIS,
                projectId,
                mixedEmployee,
                Set.of(leadId)
        );

        MonthEndTask projectLevelTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.ABRECHNUNG,
                projectId,
                null,
                Set.of(UserId.generate())
        ).completeBySystem();

        when(monthEndTaskRepository.findByMonth(month))
                .thenReturn(List.of(doneTaskA, doneTaskB, doneMixedTask, openMixedTask, projectLevelTask));

        Set<UserId> result = adapter.findUsersWithAllTasksCompleted(month);

        assertThat(result).containsExactly(allDoneEmployee);
    }

    @Test
    void findUsersWithAllTasksCompleted_shouldReturnEmptySetWhenMonthHasNoTasks() {
        YearMonth month = YearMonth.of(2026, 4);
        when(monthEndTaskRepository.findByMonth(month)).thenReturn(List.of());

        Set<UserId> result = adapter.findUsersWithAllTasksCompleted(month);

        assertThat(result).isEmpty();
    }
}
