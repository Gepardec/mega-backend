package com.gepardec.mega.hexagon.monthend.adapter.inbound;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.GenerateMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.shared.domain.util.OfficeCalendarUtil;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.YearMonth;

@ApplicationScoped
public class MonthEndTaskGenerationScheduler {

    private final GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase;

    @Inject
    public MonthEndTaskGenerationScheduler(GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase) {
        this.generateMonthEndTasksUseCase = generateMonthEndTasksUseCase;
    }

    @Scheduled(
            identity = "Generate month-end tasks on the last working day of the month at 00:00",
            cron = "0 0 0 25-31 * ? *"
    )
    void generateMonthEndTasks() {
        LocalDate today = LocalDate.now();
        if (!OfficeCalendarUtil.isLastWorkingDayOfMonth(today)) {
            Log.debugf(
                    "Skipping scheduled month-end task generation for %s because it is not the last working day of the month",
                    today);
            return;
        }

        YearMonth month = YearMonth.from(today);
        Log.infof("Starting scheduled month-end task generation for %s", month);

        MonthEndTaskGenerationResult result = generateMonthEndTasksUseCase.generate(month);
        Log.infof("Scheduled month-end task generation finished for %s: created=%d skipped=%d",
                result.month(), result.created(), result.skipped());
    }
}
