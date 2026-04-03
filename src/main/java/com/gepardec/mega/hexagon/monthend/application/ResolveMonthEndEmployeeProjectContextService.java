package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndEmployeeContextNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndEmployeeNotAssignedToProjectException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndProjectContextNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectAssignmentPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class ResolveMonthEndEmployeeProjectContextService {

    private final MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private final MonthEndUserSnapshotPort monthEndUserSnapshotPort;
    private final MonthEndProjectAssignmentPort monthEndProjectAssignmentPort;

    @Inject
    public ResolveMonthEndEmployeeProjectContextService(
            MonthEndProjectSnapshotPort monthEndProjectSnapshotPort,
            MonthEndUserSnapshotPort monthEndUserSnapshotPort,
            MonthEndProjectAssignmentPort monthEndProjectAssignmentPort
    ) {
        this.monthEndProjectSnapshotPort = monthEndProjectSnapshotPort;
        this.monthEndUserSnapshotPort = monthEndUserSnapshotPort;
        this.monthEndProjectAssignmentPort = monthEndProjectAssignmentPort;
    }

    public MonthEndEmployeeProjectContext resolve(YearMonth month, ProjectId projectId, UserId subjectEmployeeId) {
        MonthEndProjectSnapshot project = monthEndProjectSnapshotPort.findAll().stream()
                .filter(candidate -> candidate.id().equals(projectId))
                .filter(candidate -> candidate.isActiveIn(month))
                .findFirst()
                .orElseThrow(() -> new MonthEndProjectContextNotFoundException(
                        "month-end project context not found for project %s in %s".formatted(projectId.value(), month)
                ));

        Map<UserId, MonthEndUserSnapshot> activeUsersById = monthEndUserSnapshotPort.findAll().stream()
                .filter(user -> user.isActiveIn(month))
                .collect(Collectors.toMap(
                        MonthEndUserSnapshot::id,
                        Function.identity(),
                        (left, right) -> left
                ));

        MonthEndUserSnapshot subjectEmployee = activeUsersById.get(subjectEmployeeId);
        if (subjectEmployee == null) {
            throw new MonthEndEmployeeContextNotFoundException(
                    "month-end employee context not found for employee %s in %s".formatted(subjectEmployeeId.value(), month)
            );
        }

        if (!monthEndProjectAssignmentPort.findAssignedUsernames(project.zepId(), month).contains(subjectEmployee.zepUsername())) {
            throw new MonthEndEmployeeNotAssignedToProjectException(
                    "employee %s is not assigned to project %s in %s"
                            .formatted(subjectEmployeeId.value(), projectId.value(), month)
            );
        }

        return new MonthEndEmployeeProjectContext(
                month,
                project,
                subjectEmployee,
                project.leadIds().stream()
                        .filter(activeUsersById::containsKey)
                        .collect(Collectors.toSet())
        );
    }
}
