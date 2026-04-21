package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationCreatedEvent;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndEmployeeProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.monthend.domain.model.SourceSystem;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.YearMonth;

@ApplicationScoped
public class PersistZepClarificationService {

    private final MonthEndClarificationRepository monthEndClarificationRepository;
    private final Clock clock;
    private final Event<ClarificationCreatedEvent> clarificationCreatedEvent;

    @Inject
    public PersistZepClarificationService(
            MonthEndClarificationRepository monthEndClarificationRepository,
            Clock clock,
            Event<ClarificationCreatedEvent> clarificationCreatedEvent
    ) {
        this.monthEndClarificationRepository = monthEndClarificationRepository;
        this.clock = clock;
        this.clarificationCreatedEvent = clarificationCreatedEvent;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void persist(
            YearMonth month,
            MonthEndEmployeeProjectContext context,
            UserId creatorId,
            String text
    ) {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                context.project().id(),
                context.subjectEmployee().id(),
                creatorId,
                SourceSystem.ZEP,
                context.eligibleProjectLeadIds(),
                text,
                clock.instant()
        );
        monthEndClarificationRepository.save(clarification);
        clarificationCreatedEvent.fire(new ClarificationCreatedEvent(
                clarification.id(),
                clarification.sourceSystem(),
                clarification.createdBy(),
                clarification.subjectEmployeeId(),
                clarification.text()
        ));
    }
}
