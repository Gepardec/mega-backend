package com.gepardec.mega.hexagon.monthend.adapter.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GenerateMonthEndTasksUseCase;
import io.quarkus.scheduler.Scheduled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthEndTaskGenerationSchedulerTest {

    @Mock
    GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase;

    @InjectMocks
    MonthEndTaskGenerationScheduler scheduler;

    @Test
    void generateMonthEndTasks_shouldGenerateTasksForCurrentMonth() {
        when(generateMonthEndTasksUseCase.generate(any()))
                .thenReturn(new MonthEndTaskGenerationResult(YearMonth.of(2026, 3), 0, 0));

        scheduler.generateMonthEndTasks();

        ArgumentCaptor<YearMonth> monthCaptor = ArgumentCaptor.forClass(YearMonth.class);
        verify(generateMonthEndTasksUseCase).generate(monthCaptor.capture());
        assertThat(monthCaptor.getValue()).isEqualTo(YearMonth.now());
    }

    @Test
    void generateMonthEndTasks_shouldRunOnLastDayOfMonth() throws NoSuchMethodException {
        Scheduled scheduled = MonthEndTaskGenerationScheduler.class
                .getDeclaredMethod("generateMonthEndTasks")
                .getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("0 0 0 L * ? *");
    }
}
