package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.UpdateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndClarificationNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationUpdatedEvent;
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
public class UpdateMonthEndClarificationService implements UpdateMonthEndClarificationUseCase {

    private final MonthEndClarificationRepository monthEndClarificationRepository;
    private final Clock clock;
    private final Event<ClarificationUpdatedEvent> clarificationUpdatedEvent;

    @Inject
    public UpdateMonthEndClarificationService(
            MonthEndClarificationRepository monthEndClarificationRepository,
            Clock clock,
            Event<ClarificationUpdatedEvent> clarificationUpdatedEvent
    ) {
        this.monthEndClarificationRepository = monthEndClarificationRepository;
        this.clock = clock;
        this.clarificationUpdatedEvent = clarificationUpdatedEvent;
    }

    @Override
    public MonthEndClarification updateText(MonthEndClarificationId clarificationId, UserId actorId, String text) {
        MonthEndClarification clarification = monthEndClarificationRepository.findById(clarificationId)
                .orElseThrow(() -> new MonthEndClarificationNotFoundException(
                        "month-end clarification not found: " + clarificationId.value()
                ));

        MonthEndClarification updatedClarification = clarification.editText(actorId, text, clock.instant());
        monthEndClarificationRepository.save(updatedClarification);
        clarificationUpdatedEvent.fire(new ClarificationUpdatedEvent(
                updatedClarification.id(),
                actorId,
                updatedClarification.subjectEmployeeId(),
                updatedClarification.text()
        ));
        Log.infof("Updated month-end clarification %s", clarificationId.value());
        return updatedClarification;
    }
}
