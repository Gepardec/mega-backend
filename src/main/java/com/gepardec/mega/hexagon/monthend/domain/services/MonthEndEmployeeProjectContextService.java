package com.gepardec.mega.hexagon.monthend.domain.services;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndEmployeeContextNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndEmployeeNotAssignedToProjectException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndProjectContextNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndEmployeeProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectAssignmentPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class MonthEndEmployeeProjectContextService {

    private final MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private final MonthEndUserSnapshotPort monthEndUserSnapshotPort;
    private final MonthEndProjectAssignmentPort monthEndProjectAssignmentPort;

    @Inject
    public MonthEndEmployeeProjectContextService(
            MonthEndProjectSnapshotPort monthEndProjectSnapshotPort,
            MonthEndUserSnapshotPort monthEndUserSnapshotPort,
            MonthEndProjectAssignmentPort monthEndProjectAssignmentPort
    ) {
        this.monthEndProjectSnapshotPort = monthEndProjectSnapshotPort;
        this.monthEndUserSnapshotPort = monthEndUserSnapshotPort;
        this.monthEndProjectAssignmentPort = monthEndProjectAssignmentPort;
    }

    public MonthEndEmployeeProjectContext resolve(YearMonth month, ProjectId projectId, UserId subjectEmployeeId) {
        MonthEndProjectSnapshot project = monthEndProjectSnapshotPort.findActiveIn(month).stream()
                .filter(candidate -> candidate.id().equals(projectId))
                .findFirst()
                .orElseThrow(() -> new MonthEndProjectContextNotFoundException(
                        "month-end project context not found for project %s in %s".formatted(projectId.value(), month)
                ));

        Map<UserId, UserRef> activeUsersById = monthEndUserSnapshotPort.findActiveIn(month).stream()
                .collect(Collectors.toMap(
                        UserRef::id,
                        Function.identity(),
                        (left, right) -> left
                ));

        UserRef subjectEmployee = activeUsersById.get(subjectEmployeeId);
        if (subjectEmployee == null) {
            throw new MonthEndEmployeeContextNotFoundException(
                    "month-end employee context not found for employee %s in %s".formatted(subjectEmployeeId.value(), month)
            );
        }

        if (!monthEndProjectAssignmentPort.findAssignedUsernames(project.zepId(), month)
                .contains(subjectEmployee.zepUsername().value())) {
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
