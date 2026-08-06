package com.gepardec.mega.hexagon.monthend.adapter.inbound;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CloseLeistungsnachweisTasksForProjectUseCase;
import com.gepardec.mega.hexagon.project.domain.event.LeistungsnachweisDisabledEvent;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.time.Clock;
import java.time.YearMonth;

@ApplicationScoped
public class LeistungsnachweisDisabledTaskCloser {

    private final CloseLeistungsnachweisTasksForProjectUseCase closeLeistungsnachweisTasksForProjectUseCase;
    private final Clock clock;

    @Inject
    public LeistungsnachweisDisabledTaskCloser(
            CloseLeistungsnachweisTasksForProjectUseCase closeLeistungsnachweisTasksForProjectUseCase,
            Clock clock
    ) {
        this.closeLeistungsnachweisTasksForProjectUseCase = closeLeistungsnachweisTasksForProjectUseCase;
        this.clock = clock;
    }

    void onLeistungsnachweisDisabled(@Observes LeistungsnachweisDisabledEvent event) {
        YearMonth currentMonth = YearMonth.from(clock.instant().atZone(clock.getZone()));
        Log.infof("Closing open LEISTUNGSNACHWEIS tasks for project %s in %s",
                event.projectId().value(), currentMonth);
        closeLeistungsnachweisTasksForProjectUseCase.closeOpenTasks(event.projectId(), currentMonth);
    }
}
