package com.gepardec.mega.hexagon.monthend.adapter.inbound;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.GenerateMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.shared.domain.util.OfficeCalendarUtil;
import io.quarkus.scheduler.Scheduled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthEndTaskGenerationSchedulerTest {

    @Mock
    GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase;

    @InjectMocks
    MonthEndTaskGenerationScheduler scheduler;

    @Test
    void generateMonthEndTasks_shouldGenerateTasksForCurrentMonth_whenTodayIsLastWorkingDay() {
        try (MockedStatic<OfficeCalendarUtil> officeCalendarUtilMock = Mockito.mockStatic(OfficeCalendarUtil.class)) {
            officeCalendarUtilMock.when(() -> OfficeCalendarUtil.isLastWorkingDayOfMonth(any(LocalDate.class)))
                    .thenReturn(true);
            when(generateMonthEndTasksUseCase.generate(any()))
                    .thenReturn(new MonthEndTaskGenerationResult(YearMonth.now(), 0, 0));

            scheduler.generateMonthEndTasks();

            ArgumentCaptor<YearMonth> monthCaptor = ArgumentCaptor.forClass(YearMonth.class);
            verify(generateMonthEndTasksUseCase).generate(monthCaptor.capture());
            assertThat(monthCaptor.getValue()).isEqualTo(YearMonth.now());
        }
    }

    @Test
    void generateMonthEndTasks_shouldSkipTaskGeneration_whenTodayIsNotLastWorkingDay() {
        try (MockedStatic<OfficeCalendarUtil> officeCalendarUtilMock = Mockito.mockStatic(OfficeCalendarUtil.class)) {
            officeCalendarUtilMock.when(() -> OfficeCalendarUtil.isLastWorkingDayOfMonth(any(LocalDate.class)))
                    .thenReturn(false);

            scheduler.generateMonthEndTasks();

            verifyNoInteractions(generateMonthEndTasksUseCase);
        }
    }

    @Test
    void generateMonthEndTasks_shouldRunDuringLastWeekOfMonth() throws NoSuchMethodException {
        Scheduled scheduled = MonthEndTaskGenerationScheduler.class
                .getDeclaredMethod("generateMonthEndTasks")
                .getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("0 0 0 25-31 * ? *");
    }
}
