package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskKey;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GenerateMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectAssignmentPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class GenerateMonthEndTasksService implements GenerateMonthEndTasksUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;
    private final MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private final MonthEndUserSnapshotPort monthEndUserSnapshotPort;
    private final MonthEndProjectAssignmentPort monthEndProjectAssignmentPort;

    @Inject
    public GenerateMonthEndTasksService(
            MonthEndTaskRepository monthEndTaskRepository,
            MonthEndProjectSnapshotPort monthEndProjectSnapshotPort,
            MonthEndUserSnapshotPort monthEndUserSnapshotPort,
            MonthEndProjectAssignmentPort monthEndProjectAssignmentPort
    ) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.monthEndProjectSnapshotPort = monthEndProjectSnapshotPort;
        this.monthEndUserSnapshotPort = monthEndUserSnapshotPort;
        this.monthEndProjectAssignmentPort = monthEndProjectAssignmentPort;
    }

    @Override
    public MonthEndTaskGenerationResult generate(YearMonth month) {
        Objects.requireNonNull(month, "month must not be null");
        Log.infof("Generating month-end tasks for %s", month);

        Set<MonthEndTaskKey> existingKeys = monthEndTaskRepository.findByMonth(month).stream()
                .map(MonthEndTask::businessKey)
                .collect(Collectors.toCollection(HashSet::new));

        Map<String, MonthEndUserSnapshot> activeUsersByUsername = monthEndUserSnapshotPort.findAll().stream()
                .filter(user -> user.isActiveIn(month))
                .collect(Collectors.toMap(
                        MonthEndUserSnapshot::zepUsername,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        Set<UserId> activeUserIds = activeUsersByUsername.values().stream()
                .map(MonthEndUserSnapshot::id)
                .collect(Collectors.toCollection(HashSet::new));

        List<MonthEndTask> tasksToCreate = new ArrayList<>();
        int skipped = 0;

        for (MonthEndProjectSnapshot project : monthEndProjectSnapshotPort.findAll()) {
            if (!project.isActiveIn(month)) {
                continue;
            }

            Set<UserId> activeLeadIds = project.leadIds().stream()
                    .filter(activeUserIds::contains)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Set<MonthEndUserSnapshot> assignedUsers = monthEndProjectAssignmentPort.findAssignedUsernames(project.zepId(), month).stream()
                    .map(activeUsersByUsername::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            for (MonthEndUserSnapshot assignedUser : assignedUsers) {
                skipped += addTaskIfMissing(
                        existingKeys,
                        tasksToCreate,
                        MonthEndTask.create(
                                MonthEndTaskId.generate(),
                                month,
                                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                                project.id(),
                                assignedUser.id(),
                                Set.of(assignedUser.id())
                        )
                );

                if (project.billable()) {
                    skipped += addTaskIfMissing(
                            existingKeys,
                            tasksToCreate,
                            MonthEndTask.create(
                                    MonthEndTaskId.generate(),
                                    month,
                                    MonthEndTaskType.LEISTUNGSNACHWEIS,
                                    project.id(),
                                    assignedUser.id(),
                                    Set.of(assignedUser.id())
                            )
                    );
                }

                if (!activeLeadIds.isEmpty()) {
                    skipped += addTaskIfMissing(
                            existingKeys,
                            tasksToCreate,
                            MonthEndTask.create(
                                    MonthEndTaskId.generate(),
                                    month,
                                    MonthEndTaskType.PROJECT_LEAD_REVIEW,
                                    project.id(),
                                    assignedUser.id(),
                                    activeLeadIds
                            )
                    );
                }
            }

            if (project.billable() && !activeLeadIds.isEmpty()) {
                skipped += addTaskIfMissing(
                        existingKeys,
                        tasksToCreate,
                        MonthEndTask.create(
                                MonthEndTaskId.generate(),
                                month,
                                MonthEndTaskType.ABRECHNUNG,
                                project.id(),
                                null,
                                activeLeadIds
                        )
                );
            }
        }

        monthEndTaskRepository.saveAll(tasksToCreate);

        Log.infof("Generation finished for %s: created=%d skipped=%d",
                month, tasksToCreate.size(), skipped);
        return new MonthEndTaskGenerationResult(month, tasksToCreate.size(), skipped);
    }

    private int addTaskIfMissing(
            Set<MonthEndTaskKey> existingKeys,
            List<MonthEndTask> tasksToCreate,
            MonthEndTask candidate
    ) {
        if (!existingKeys.add(candidate.businessKey())) {
            return 1;
        }
        tasksToCreate.add(candidate);
        return 0;
    }
}
