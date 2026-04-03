package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndClarificationNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.UpdateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;

@ApplicationScoped
@Transactional
public class UpdateMonthEndClarificationService implements UpdateMonthEndClarificationUseCase {

    private final MonthEndClarificationRepository monthEndClarificationRepository;
    private final Clock clock;

    @Inject
    public UpdateMonthEndClarificationService(
            MonthEndClarificationRepository monthEndClarificationRepository,
            Clock clock
    ) {
        this.monthEndClarificationRepository = monthEndClarificationRepository;
        this.clock = clock;
    }

    @Override
    public MonthEndClarification updateText(MonthEndClarificationId clarificationId, UserId actorId, String text) {
        MonthEndClarification clarification = monthEndClarificationRepository.findById(clarificationId)
                .orElseThrow(() -> new MonthEndClarificationNotFoundException(
                        "month-end clarification not found: " + clarificationId.value()
                ));

        MonthEndClarification updatedClarification = clarification.editText(actorId, text, clock.instant());
        monthEndClarificationRepository.save(updatedClarification);
        Log.infof("Updated month-end clarification %s", clarificationId.value());
        return updatedClarification;
    }
}
