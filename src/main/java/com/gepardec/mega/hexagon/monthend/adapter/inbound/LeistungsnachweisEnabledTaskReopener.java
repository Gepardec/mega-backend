package com.gepardec.mega.hexagon.monthend.adapter.inbound;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.ReopenLeistungsnachweisTasksForProjectUseCase;
import com.gepardec.mega.hexagon.project.domain.event.LeistungsnachweisEnabledEvent;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.time.Clock;
import java.time.YearMonth;

@ApplicationScoped
public class LeistungsnachweisEnabledTaskReopener {

    private final ReopenLeistungsnachweisTasksForProjectUseCase reopenLeistungsnachweisTasksForProjectUseCase;
    private final Clock clock;

    @Inject
    public LeistungsnachweisEnabledTaskReopener(
            ReopenLeistungsnachweisTasksForProjectUseCase reopenLeistungsnachweisTasksForProjectUseCase,
            Clock clock
    ) {
        this.reopenLeistungsnachweisTasksForProjectUseCase = reopenLeistungsnachweisTasksForProjectUseCase;
        this.clock = clock;
    }

    void onLeistungsnachweisEnabled(@Observes LeistungsnachweisEnabledEvent event) {
        YearMonth currentMonth = YearMonth.from(clock.instant().atZone(clock.getZone()));
        YearMonth previousMonth = currentMonth.minusMonths(1);

        Log.infof("Reopening closed LEISTUNGSNACHWEIS tasks for project %s in current month %s and previous month %s",
                event.projectId().value(), currentMonth, previousMonth);

        reopenLeistungsnachweisTasksForProjectUseCase.reopenClosedTasks(event.projectId(), currentMonth);
        reopenLeistungsnachweisTasksForProjectUseCase.reopenClosedTasks(event.projectId(), previousMonth);
    }
}
