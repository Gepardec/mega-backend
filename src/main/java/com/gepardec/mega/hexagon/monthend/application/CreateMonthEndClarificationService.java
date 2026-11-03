package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationCreatedEvent;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.model.SourceSystem;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.YearMonth;
import java.util.Objects;

@ApplicationScoped
@Transactional
public class CreateMonthEndClarificationService implements CreateMonthEndClarificationUseCase {

    private final MonthEndClarificationRepository monthEndClarificationRepository;
    private final MonthEndEmployeeProjectContextService employeeContextService;
    private final MonthEndProjectContextService projectContextService;
    private final Clock clock;
    private final Event<ClarificationCreatedEvent> clarificationCreatedEvent;

    @Inject
    public CreateMonthEndClarificationService(
            MonthEndClarificationRepository monthEndClarificationRepository,
            MonthEndEmployeeProjectContextService employeeContextService,
            MonthEndProjectContextService projectContextService,
            Clock clock,
            Event<ClarificationCreatedEvent> clarificationCreatedEvent
    ) {
        this.monthEndClarificationRepository = monthEndClarificationRepository;
        this.employeeContextService = employeeContextService;
        this.projectContextService = projectContextService;
        this.clock = clock;
        this.clarificationCreatedEvent = clarificationCreatedEvent;
    }

    @Override
    public MonthEndClarification create(
            YearMonth month,
            ProjectId projectId,
            UserId subjectEmployeeId,
            UserId actorId,
            SourceSystem sourceSystem,
            String text
    ) {
        Objects.requireNonNull(month, "month must not be null");
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(actorId, "actorId must not be null");
        Objects.requireNonNull(sourceSystem, "sourceSystem must not be null");

        MonthEndProjectContext projectContext = projectContextService.resolve(month, projectId);

        // The factory enforces who may create; it runs before the subject checks so a caller who
        // is not allowed gets 403 even when the named employee is not assigned.
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                subjectEmployeeId,
                actorId,
                sourceSystem,
                projectContext.eligibleProjectLeadIds(),
                text,
                clock.instant()
        );
        if (subjectEmployeeId != null) {
            employeeContextService.resolve(projectContext, subjectEmployeeId);
        }
        monthEndClarificationRepository.save(clarification);
        clarificationCreatedEvent.fire(new ClarificationCreatedEvent(
                clarification.id(),
                clarification.sourceSystem(),
                clarification.createdBy(),
                clarification.subjectEmployeeId(),
                clarification.text(),
                clarification.eligibleProjectLeadIds()
        ));

        Log.infof(
                "Created month-end clarification %s for project %s, employee %s, month %s",
                clarification.id().value(),
                projectId.value(),
                subjectEmployeeId != null ? subjectEmployeeId.value() : "project-level",
                month
        );
        return clarification;
    }
}
