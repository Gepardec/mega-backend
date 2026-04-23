package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.PrematureMonthEndPreparationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndEmployeeContextNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndValidationException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectAssignmentPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.services.MonthEndTaskPlanningService;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class PrematureMonthEndPreparationService implements PrematureMonthEndPreparationUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;
    private final MonthEndTaskPlanningService monthEndTaskPlanningService;
    private final MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private final MonthEndUserSnapshotPort monthEndUserSnapshotPort;
    private final MonthEndProjectAssignmentPort monthEndProjectAssignmentPort;
    private final MonthEndClarificationRepository monthEndClarificationRepository;
    private final Clock clock;

    @Inject
    public PrematureMonthEndPreparationService(
            MonthEndTaskRepository monthEndTaskRepository,
            MonthEndTaskPlanningService monthEndTaskPlanningService,
            MonthEndProjectSnapshotPort monthEndProjectSnapshotPort,
            MonthEndUserSnapshotPort monthEndUserSnapshotPort,
            MonthEndProjectAssignmentPort monthEndProjectAssignmentPort,
            MonthEndClarificationRepository monthEndClarificationRepository,
            Clock clock
    ) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.monthEndTaskPlanningService = monthEndTaskPlanningService;
        this.monthEndProjectSnapshotPort = monthEndProjectSnapshotPort;
        this.monthEndUserSnapshotPort = monthEndUserSnapshotPort;
        this.monthEndProjectAssignmentPort = monthEndProjectAssignmentPort;
        this.monthEndClarificationRepository = monthEndClarificationRepository;
        this.clock = clock;
    }

    @Override
    public void prepare(
            YearMonth month,
            UserId actorId,
            String clarificationText
    ) {
        Objects.requireNonNull(month, "month must not be null");
        Objects.requireNonNull(actorId, "actorId must not be null");
        String requiredClarificationText = requireNonBlank(clarificationText);

        Map<UserId, UserRef> activeUsersById = monthEndUserSnapshotPort.findActiveIn(month).stream()
                .collect(Collectors.toMap(
                        UserRef::id,
                        Function.identity()
                ));

        UserRef actor = activeUsersById.get(actorId);
        if (actor == null) {
            throw new MonthEndEmployeeContextNotFoundException(
                    "month-end employee context not found for employee %s in %s".formatted(actorId.value(), month)
            );
        }

        List<MonthEndProjectSnapshot> relevantProjects = monthEndProjectSnapshotPort.findActiveIn(month).stream()
                .filter(project -> isAssignedTo(project, month, actor))
                .filter(project -> !monthEndTaskRepository.existsForSubjectEmployee(month, project.id(), actorId))
                .toList();

        int totalTasks = 0;
        int projectsNewlyPrepared = 0;

        for (MonthEndProjectSnapshot project : relevantProjects) {
            List<MonthEndTask> tasks = monthEndTaskPlanningService.planEmployeeOwnedTasks(month, project, actor);
            tasks.forEach(monthEndTaskRepository::save);

            Set<UserId> eligibleLeadIds = project.leadIds().stream()
                    .filter(activeUsersById::containsKey)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            monthEndClarificationRepository.save(MonthEndClarification.create(
                    MonthEndClarificationId.generate(),
                    month,
                    project.id(),
                    actorId,
                    actorId,
                    eligibleLeadIds,
                    requiredClarificationText,
                    clock.instant()
            ));

            totalTasks += tasks.size();
            projectsNewlyPrepared++;
        }

        Log.infof(
                "Employee %s prematurely prepared %d tasks across %d projects for month %s",
                actorId.value(),
                totalTasks,
                projectsNewlyPrepared,
                month
        );
    }

    private boolean isAssignedTo(MonthEndProjectSnapshot project, YearMonth month, UserRef actor) {
        return monthEndProjectAssignmentPort.findAssignedUsernames(project.zepId(), month)
                .contains(actor.zepUsername().value());
    }

    private String requireNonBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new MonthEndValidationException("clarificationText must not be blank");
        }
        return value;
    }
}
