package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndClarificationNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationCompletedEvent;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;

@ApplicationScoped
@Transactional
public class CompleteMonthEndClarificationService implements CompleteMonthEndClarificationUseCase {

    private final MonthEndClarificationRepository monthEndClarificationRepository;
    private final Clock clock;
    private final Event<ClarificationCompletedEvent> clarificationCompletedEvent;

    @Inject
    public CompleteMonthEndClarificationService(
            MonthEndClarificationRepository monthEndClarificationRepository,
            Clock clock,
            Event<ClarificationCompletedEvent> clarificationCompletedEvent
    ) {
        this.monthEndClarificationRepository = monthEndClarificationRepository;
        this.clock = clock;
        this.clarificationCompletedEvent = clarificationCompletedEvent;
    }

    @Override
    public MonthEndClarification complete(MonthEndClarificationId clarificationId, UserId actorId, String resolutionNote) {
        MonthEndClarification clarification = monthEndClarificationRepository.findById(clarificationId)
                .orElseThrow(() -> new MonthEndClarificationNotFoundException(
                        "month-end clarification not found: " + clarificationId.value()
                ));

        MonthEndClarification completedClarification = clarification.resolve(actorId, resolutionNote, clock.instant());
        if (!completedClarification.equals(clarification)) {
            monthEndClarificationRepository.save(completedClarification);
            clarificationCompletedEvent.fire(new ClarificationCompletedEvent(
                    completedClarification.id(),
                    completedClarification.createdBy(),
                    completedClarification.subjectEmployeeId(),
                    completedClarification.text(),
                    completedClarification.resolvedBy()
            ));
            Log.infof("Completed month-end clarification %s by actor %s", clarificationId.value(), actorId.value());
        }

        return completedClarification;
    }
}
