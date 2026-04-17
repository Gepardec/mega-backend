package com.gepardec.mega.hexagon.monthend.domain.services;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndProjectContextNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class MonthEndProjectContextService {

    private final MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private final MonthEndUserSnapshotPort monthEndUserSnapshotPort;

    @Inject
    public MonthEndProjectContextService(
            MonthEndProjectSnapshotPort monthEndProjectSnapshotPort,
            MonthEndUserSnapshotPort monthEndUserSnapshotPort
    ) {
        this.monthEndProjectSnapshotPort = monthEndProjectSnapshotPort;
        this.monthEndUserSnapshotPort = monthEndUserSnapshotPort;
    }

    public MonthEndProjectContext resolve(YearMonth month, ProjectId projectId) {
        MonthEndProjectSnapshot project = monthEndProjectSnapshotPort.findActiveIn(month).stream()
                .filter(candidate -> candidate.id().equals(projectId))
                .findFirst()
                .orElseThrow(() -> new MonthEndProjectContextNotFoundException(
                        "month-end project context not found for project %s in %s".formatted(projectId.value(), month)
                ));

        Set<UserId> activeUserIds = monthEndUserSnapshotPort.findActiveIn(month).stream()
                .map(UserRef::id)
                .collect(Collectors.toSet());

        Set<UserId> eligibleLeadIds = project.leadIds().stream()
                .filter(activeUserIds::contains)
                .collect(Collectors.toSet());

        return new MonthEndProjectContext(month, project, eligibleLeadIds);
    }
}
