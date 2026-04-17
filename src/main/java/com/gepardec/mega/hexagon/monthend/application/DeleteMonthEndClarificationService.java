package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.DeleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndActorNotAuthorizedException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndClarificationClosedException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndClarificationNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
public class DeleteMonthEndClarificationService implements DeleteMonthEndClarificationUseCase {

    private final MonthEndClarificationRepository monthEndClarificationRepository;

    @Inject
    public DeleteMonthEndClarificationService(MonthEndClarificationRepository monthEndClarificationRepository) {
        this.monthEndClarificationRepository = monthEndClarificationRepository;
    }

    @Override
    public void delete(MonthEndClarificationId id, UserId actorId) {
        MonthEndClarification clarification = monthEndClarificationRepository.findById(id)
                .orElseThrow(() -> new MonthEndClarificationNotFoundException(
                        "month-end clarification not found: " + id.value()
                ));

        if (!actorId.equals(clarification.createdBy())) {
            throw new MonthEndActorNotAuthorizedException("actor is not allowed to delete this clarification");
        }

        if (!clarification.isOpen()) {
            throw new MonthEndClarificationClosedException("done clarifications cannot be deleted");
        }

        monthEndClarificationRepository.delete(id);
        Log.infof("Deleted month-end clarification %s by actor %s", id.value(), actorId.value());
    }
}
