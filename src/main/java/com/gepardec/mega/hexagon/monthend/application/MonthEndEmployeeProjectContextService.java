package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.outbound.MonthEndProjectAssignmentPort;
import com.gepardec.mega.hexagon.monthend.application.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndEmployeeContextNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndEmployeeNotAssignedToProjectException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndEmployeeProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.shared.domain.SystemActor;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.Set;

@ApplicationScoped
public class MonthEndEmployeeProjectContextService {

    private final MonthEndProjectContextService projectContextService;
    private final MonthEndUserSnapshotPort monthEndUserSnapshotPort;
    private final MonthEndProjectAssignmentPort monthEndProjectAssignmentPort;

    @Inject
    public MonthEndEmployeeProjectContextService(
            MonthEndProjectContextService projectContextService,
            MonthEndUserSnapshotPort monthEndUserSnapshotPort,
            MonthEndProjectAssignmentPort monthEndProjectAssignmentPort
    ) {
        this.projectContextService = projectContextService;
        this.monthEndUserSnapshotPort = monthEndUserSnapshotPort;
        this.monthEndProjectAssignmentPort = monthEndProjectAssignmentPort;
    }

    public MonthEndEmployeeProjectContext resolve(YearMonth month, ProjectId projectId, UserId subjectEmployeeId) {
        return resolve(projectContextService.resolve(month, projectId), subjectEmployeeId);
    }

    /**
     * Builds the employee context on top of an already resolved project context, so the project and
     * active user snapshots are not loaded a second time. Only the subject is looked up.
     */
    public MonthEndEmployeeProjectContext resolve(MonthEndProjectContext projectContext, UserId subjectEmployeeId) {
        YearMonth month = projectContext.month();
        MonthEndProjectSnapshot project = projectContext.project();

        UserRef subjectEmployee = monthEndUserSnapshotPort.findByIds(Set.of(subjectEmployeeId), month).stream()
                .filter(user -> user.id().equals(subjectEmployeeId))
                .filter(user -> !SystemActor.USER_ID.equals(user.id()))
                .findFirst()
                .orElseThrow(() -> new MonthEndEmployeeContextNotFoundException(
                        "month-end employee context not found for employee %s in %s".formatted(subjectEmployeeId.value(), month)
                ));

        if (!monthEndProjectAssignmentPort.findAssignedUsernames(project.zepId(), month)
                .contains(subjectEmployee.zepUsername().value())) {
            throw new MonthEndEmployeeNotAssignedToProjectException(
                    "employee %s is not assigned to project %s in %s"
                            .formatted(subjectEmployeeId.value(), project.id().value(), month)
            );
        }

        return new MonthEndEmployeeProjectContext(
                month,
                project,
                subjectEmployee,
                projectContext.eligibleProjectLeadIds()
        );
    }
}
