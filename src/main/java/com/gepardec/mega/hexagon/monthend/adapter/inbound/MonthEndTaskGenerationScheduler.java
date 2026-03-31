package com.gepardec.mega.hexagon.monthend.adapter.inbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GenerateMonthEndTasksUseCase;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;

@ApplicationScoped
public class MonthEndTaskGenerationScheduler {

    private final GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase;

    @Inject
    public MonthEndTaskGenerationScheduler(GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase) {
        this.generateMonthEndTasksUseCase = generateMonthEndTasksUseCase;
    }

    @Scheduled(
            identity = "Generate month-end tasks on the last day of the month at 00:00",
            cron = "0 0 0 L * ? *"
    )
    void generateMonthEndTasks() {
        YearMonth month = YearMonth.now();
        Log.infof("[MonthEndScheduler] Starting scheduled month-end task generation for %s", month);

        MonthEndTaskGenerationResult result = generateMonthEndTasksUseCase.generate(month);
        Log.infof("[MonthEndScheduler] Scheduled month-end task generation finished for %s: created=%d skipped=%d",
                result.month(), result.created(), result.skipped());
    }
}
