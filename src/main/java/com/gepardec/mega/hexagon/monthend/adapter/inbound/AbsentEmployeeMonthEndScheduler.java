package com.gepardec.mega.hexagon.monthend.adapter.inbound;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteTasksForAbsentEmployeeUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;

@ApplicationScoped
public class AbsentEmployeeMonthEndScheduler {

    private final CompleteTasksForAbsentEmployeeUseCase completeTasksForAbsentEmployeeUseCase;
    private final MonthEndUserSnapshotPort monthEndUserSnapshotPort;

    @Inject
    public AbsentEmployeeMonthEndScheduler(
            CompleteTasksForAbsentEmployeeUseCase completeTasksForAbsentEmployeeUseCase,
            MonthEndUserSnapshotPort monthEndUserSnapshotPort
    ) {
        this.completeTasksForAbsentEmployeeUseCase = completeTasksForAbsentEmployeeUseCase;
        this.monthEndUserSnapshotPort = monthEndUserSnapshotPort;
    }

    @Scheduled(
            identity = "Auto-complete month-end tasks for fully absent employees at 17:00 on month end",
            cron = "0 0 17 L * ?"
    )
    void completeTasksForAbsentEmployees() {
        YearMonth month = YearMonth.now();
        Log.infof("Starting absent-employee month-end auto-completion for %s", month);
        for (UserRef activeUser : monthEndUserSnapshotPort.findActiveIn(month)) {
            completeTasksForAbsentEmployeeUseCase.complete(activeUser.id(), month)
                    .ifPresent(result -> Log.infof(
                            "Auto-completed month-end tasks for absent employee %s in %s",
                            result.employeeId().value(),
                            result.month()
                    ));
        }
        Log.infof("Finished absent-employee month-end auto-completion for %s", month);
    }
}
