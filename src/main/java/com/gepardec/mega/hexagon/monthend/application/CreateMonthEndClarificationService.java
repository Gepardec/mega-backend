package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndEmployeeProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.monthend.domain.services.MonthEndEmployeeProjectContextService;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.YearMonth;
import java.util.Objects;

@ApplicationScoped
@Transactional
public class CreateMonthEndClarificationService implements CreateMonthEndClarificationUseCase {

    private final MonthEndClarificationRepository monthEndClarificationRepository;
    private final MonthEndEmployeeProjectContextService contextService;
    private final Clock clock;

    @Inject
    public CreateMonthEndClarificationService(
            MonthEndClarificationRepository monthEndClarificationRepository,
            MonthEndEmployeeProjectContextService contextService,
            Clock clock
    ) {
        this.monthEndClarificationRepository = monthEndClarificationRepository;
        this.contextService = contextService;
        this.clock = clock;
    }

    @Override
    public MonthEndClarification create(
            YearMonth month,
            ProjectId projectId,
            UserId subjectEmployeeId,
            UserId actorId,
            MonthEndClarificationSide creatorSide,
            String text
    ) {
        Objects.requireNonNull(month, "month must not be null");
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(subjectEmployeeId, "subjectEmployeeId must not be null");
        Objects.requireNonNull(actorId, "actorId must not be null");
        Objects.requireNonNull(creatorSide, "creatorSide must not be null");

        MonthEndEmployeeProjectContext context = contextService.resolve(month, projectId, subjectEmployeeId);

        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                subjectEmployeeId,
                actorId,
                creatorSide,
                context.eligibleProjectLeadIds(),
                text,
                clock.instant()
        );
        monthEndClarificationRepository.save(clarification);

        Log.infof(
                "Created month-end clarification %s for project %s, employee %s, month %s",
                clarification.id().value(),
                projectId.value(),
                subjectEmployeeId.value(),
                month
        );
        return clarification;
    }
}
