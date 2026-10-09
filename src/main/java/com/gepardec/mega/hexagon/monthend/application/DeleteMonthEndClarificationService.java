package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.DeleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndErrorCode;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndException;
import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationDeletedEvent;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
public class DeleteMonthEndClarificationService implements DeleteMonthEndClarificationUseCase {

    private final MonthEndClarificationRepository monthEndClarificationRepository;
    private final Event<ClarificationDeletedEvent> clarificationDeletedEvent;

    @Inject
    public DeleteMonthEndClarificationService(
            MonthEndClarificationRepository monthEndClarificationRepository,
            Event<ClarificationDeletedEvent> clarificationDeletedEvent
    ) {
        this.monthEndClarificationRepository = monthEndClarificationRepository;
        this.clarificationDeletedEvent = clarificationDeletedEvent;
    }

    @Override
    public void delete(MonthEndClarificationId id, UserId actorId) {
        MonthEndClarification clarification = monthEndClarificationRepository.findById(id)
                .orElseThrow(() -> new MonthEndException(MonthEndErrorCode.CLARIFICATION_NOT_FOUND,
                        "month-end clarification not found: " + id.value()
                ));

        if (!actorId.equals(clarification.createdBy())) {
            throw new MonthEndException(MonthEndErrorCode.ACTOR_NOT_AUTHORIZED, "actor is not allowed to delete this clarification");
        }

        if (!clarification.isOpen()) {
            throw new MonthEndException(MonthEndErrorCode.CLARIFICATION_CLOSED, "done clarifications cannot be deleted");
        }

        monthEndClarificationRepository.delete(id);
        clarificationDeletedEvent.fire(new ClarificationDeletedEvent(
                clarification.id(),
                clarification.createdBy(),
                clarification.subjectEmployeeId(),
                clarification.text(),
                clarification.eligibleProjectLeadIds()
        ));
        Log.infof("Deleted month-end clarification %s by actor %s", id.value(), actorId.value());
    }
}
