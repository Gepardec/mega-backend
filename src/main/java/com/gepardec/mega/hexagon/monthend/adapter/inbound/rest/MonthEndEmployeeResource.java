package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.MonthEndEmployeeApi;
import com.gepardec.mega.hexagon.generated.model.CreateEmployeeClarificationRequest;
import com.gepardec.mega.hexagon.generated.model.PrepareMonthEndProjectRequest;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetEmployeeMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.PrematureMonthEndPreparationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndPreparationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequestScoped
@Authenticated
@MegaRolesAllowed(Role.EMPLOYEE)
public class MonthEndEmployeeResource implements MonthEndEmployeeApi {

    private final GetEmployeeMonthEndStatusOverviewUseCase getEmployeeMonthEndStatusOverviewUseCase;
    private final PrematureMonthEndPreparationUseCase prematureMonthEndPreparationUseCase;
    private final CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase;
    private final MonthEndProjectSnapshotPort projectSnapshotPort;
    private final MonthEndUserSnapshotPort userSnapshotPort;
    private final AuthenticatedActorContext authenticatedActorContext;
    private final MonthEndRestTransportHelper transportHelper;
    private final MonthEndRestMapper monthEndRestMapper;

    @Inject
    public MonthEndEmployeeResource(
            GetEmployeeMonthEndStatusOverviewUseCase getEmployeeMonthEndStatusOverviewUseCase,
            PrematureMonthEndPreparationUseCase prematureMonthEndPreparationUseCase,
            CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase,
            MonthEndProjectSnapshotPort projectSnapshotPort,
            MonthEndUserSnapshotPort userSnapshotPort,
            AuthenticatedActorContext authenticatedActorContext,
            MonthEndRestTransportHelper transportHelper,
            MonthEndRestMapper monthEndRestMapper
    ) {
        this.getEmployeeMonthEndStatusOverviewUseCase = getEmployeeMonthEndStatusOverviewUseCase;
        this.prematureMonthEndPreparationUseCase = prematureMonthEndPreparationUseCase;
        this.createMonthEndClarificationUseCase = createMonthEndClarificationUseCase;
        this.projectSnapshotPort = projectSnapshotPort;
        this.userSnapshotPort = userSnapshotPort;
        this.authenticatedActorContext = authenticatedActorContext;
        this.transportHelper = transportHelper;
        this.monthEndRestMapper = monthEndRestMapper;
    }

    @Override
    public Response getEmployeeMonthEndStatusOverview(String month) {
        UserId actorId = authenticatedActorContext.userId();
        MonthEndStatusOverview overview = getEmployeeMonthEndStatusOverviewUseCase.getOverview(
                actorId,
                transportHelper.parseMonth(month)
        );
        Map<ProjectId, ProjectRef> projectRefs = resolveProjectRefs(overview.tasks(), overview.month());
        Map<UserId, UserRef> userRefs = resolveUserRefs(overviewUserIds(overview), overview.month());
        return Response.ok(monthEndRestMapper.toResponse(overview, projectRefs, userRefs, actorId)).build();
    }

    @Override
    public Response createEmployeeMonthEndClarification(CreateEmployeeClarificationRequest request) {
        UserId actorId = authenticatedActorContext.userId();
        MonthEndClarification clarification = createMonthEndClarificationUseCase.create(
                transportHelper.parseMonth(request.getMonth()),
                transportHelper.toProjectId(request.getProjectId()),
                actorId,
                actorId,
                request.getText()
        );
        Map<UserId, UserRef> userRefs = resolveUserRefs(clarification.referencedUserIds(), clarification.month());
        return Response.status(Response.Status.CREATED)
                .entity(monthEndRestMapper.toClarificationEntry(clarification, userRefs, actorId))
                .build();
    }

    @Override
    public Response prepareEmployeeMonthEndProject(PrepareMonthEndProjectRequest request) {
        UserId actorId = authenticatedActorContext.userId();
        YearMonth month = transportHelper.parseMonth(request.getMonth());
        MonthEndPreparationResult result = prematureMonthEndPreparationUseCase.prepare(
                month,
                transportHelper.toProjectId(request.getProjectId()),
                actorId,
                request.getClarificationText()
        );
        Set<UserId> ids = result.hasClarification() ? result.clarification().referencedUserIds() : Set.of();
        Map<UserId, UserRef> userRefs = resolveUserRefs(ids, month);
        return Response.ok(monthEndRestMapper.toResponse(result, userRefs, actorId)).build();
    }

    private Map<ProjectId, ProjectRef> resolveProjectRefs(List<MonthEndTask> tasks, YearMonth month) {
        if (tasks.isEmpty()) {
            return Map.of();
        }
        Set<ProjectId> projectIds = tasks.stream()
                .map(MonthEndTask::projectId)
                .collect(Collectors.toSet());
        return projectSnapshotPort.findByIds(projectIds, month).stream()
                .collect(Collectors.toMap(
                        MonthEndProjectSnapshot::id,
                        snapshot -> new ProjectRef(snapshot.id(), snapshot.zepId(), snapshot.name())
                ));
    }

    private Map<UserId, UserRef> resolveUserRefs(Set<UserId> ids, YearMonth month) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userSnapshotPort.findByIds(ids, month).stream()
                .collect(Collectors.toMap(UserRef::id, Function.identity()));
    }

    private static Set<UserId> overviewUserIds(MonthEndStatusOverview overview) {
        return Stream.concat(
                overview.tasks().stream()
                        .map(MonthEndTask::subjectEmployeeId)
                        .filter(Objects::nonNull),
                overview.clarifications().stream()
                        .flatMap(c -> c.referencedUserIds().stream())
        ).collect(Collectors.toSet());
    }
}
