package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.PrematureMonthEndPreparationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndEmployeeProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndPreparationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.monthend.domain.services.MonthEndEmployeeProjectContextService;
import com.gepardec.mega.hexagon.monthend.domain.services.MonthEndTaskPlanningService;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
@Transactional
public class PrematureMonthEndPreparationService implements PrematureMonthEndPreparationUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;
    private final MonthEndTaskPlanningService monthEndTaskPlanningService;
    private final MonthEndEmployeeProjectContextService contextService;
    private final CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase;

    @Inject
    public PrematureMonthEndPreparationService(
            MonthEndTaskRepository monthEndTaskRepository,
            MonthEndTaskPlanningService monthEndTaskPlanningService,
            MonthEndEmployeeProjectContextService contextService,
            CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase
    ) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.monthEndTaskPlanningService = monthEndTaskPlanningService;
        this.contextService = contextService;
        this.createMonthEndClarificationUseCase = createMonthEndClarificationUseCase;
    }

    @Override
    public MonthEndPreparationResult prepare(
            YearMonth month,
            ProjectId projectId,
            UserId actorId,
            String clarificationText
    ) {
        Objects.requireNonNull(month, "month must not be null");
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(actorId, "actorId must not be null");

        MonthEndEmployeeProjectContext context = contextService.resolve(month, projectId, actorId);

        List<MonthEndTask> ensuredTasks = monthEndTaskPlanningService.planEmployeeOwnedTasks(
                        month,
                        context.project(),
                        context.subjectEmployee()
                ).stream()
                .map(this::ensureTask)
                .toList();

        MonthEndClarification clarification = null;
        if (clarificationText != null && !clarificationText.isBlank()) {
            clarification = createMonthEndClarificationUseCase.create(
                    month,
                    projectId,
                    context.subjectEmployee().id(),
                    actorId,
                    MonthEndClarificationSide.EMPLOYEE,
                    clarificationText
            );
        }

        Log.infof(
                "Prepared month-end obligations for actor %s, project %s, month %s: ensured=%d clarificationCreated=%s",
                actorId.value(),
                projectId.value(),
                month,
                ensuredTasks.size(),
                clarification != null
        );
        return new MonthEndPreparationResult(ensuredTasks, clarification);
    }

    private MonthEndTask ensureTask(MonthEndTask candidate) {
        return monthEndTaskRepository.findByBusinessKey(candidate.businessKey())
                .orElseGet(() -> {
                    monthEndTaskRepository.save(candidate);
                    return candidate;
                });
    }
}
