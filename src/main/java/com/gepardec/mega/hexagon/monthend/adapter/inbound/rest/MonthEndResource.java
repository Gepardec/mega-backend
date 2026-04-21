package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.MonthEndApi;
import com.gepardec.mega.hexagon.generated.model.CreateClarificationRequestDto;
import com.gepardec.mega.hexagon.generated.model.MonthEndStatusOverviewDto;
import com.gepardec.mega.hexagon.generated.model.PrepareMonthEndProjectRequestDto;
import com.gepardec.mega.hexagon.generated.model.ResolveClarificationRequestDto;
import com.gepardec.mega.hexagon.generated.model.UpdateClarificationTextRequestDto;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndTaskUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.DeleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GenerateMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetEmployeeMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetProjectLeadMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.PrematureMonthEndPreparationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.UpdateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndPreparationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import io.quarkus.oidc.Tenant;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequestScoped
@Authenticated
public class MonthEndResource implements MonthEndApi {

    private final GetEmployeeMonthEndStatusOverviewUseCase getEmployeeMonthEndStatusOverviewUseCase;
    private final GetProjectLeadMonthEndStatusOverviewUseCase getProjectLeadMonthEndStatusOverviewUseCase;
    private final PrematureMonthEndPreparationUseCase prematureMonthEndPreparationUseCase;
    private final CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase;
    private final CompleteMonthEndTaskUseCase completeMonthEndTaskUseCase;
    private final UpdateMonthEndClarificationUseCase updateMonthEndClarificationUseCase;
    private final CompleteMonthEndClarificationUseCase completeMonthEndClarificationUseCase;
    private final DeleteMonthEndClarificationUseCase deleteMonthEndClarificationUseCase;
    private final GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase;
    private final MonthEndProjectSnapshotPort projectSnapshotPort;
    private final MonthEndUserSnapshotPort userSnapshotPort;
    private final AuthenticatedActorContext authenticatedActorContext;
    private final MonthEndRestTransportHelper transportHelper;
    private final MonthEndRestMapper monthEndRestMapper;

    @Inject
    public MonthEndResource(
            GetEmployeeMonthEndStatusOverviewUseCase getEmployeeMonthEndStatusOverviewUseCase,
            GetProjectLeadMonthEndStatusOverviewUseCase getProjectLeadMonthEndStatusOverviewUseCase,
            PrematureMonthEndPreparationUseCase prematureMonthEndPreparationUseCase,
            CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase,
            CompleteMonthEndTaskUseCase completeMonthEndTaskUseCase,
            UpdateMonthEndClarificationUseCase updateMonthEndClarificationUseCase,
            CompleteMonthEndClarificationUseCase completeMonthEndClarificationUseCase,
            DeleteMonthEndClarificationUseCase deleteMonthEndClarificationUseCase,
            GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase,
            MonthEndProjectSnapshotPort projectSnapshotPort,
            MonthEndUserSnapshotPort userSnapshotPort,
            AuthenticatedActorContext authenticatedActorContext,
            MonthEndRestTransportHelper transportHelper,
            MonthEndRestMapper monthEndRestMapper
    ) {
        this.getEmployeeMonthEndStatusOverviewUseCase = getEmployeeMonthEndStatusOverviewUseCase;
        this.getProjectLeadMonthEndStatusOverviewUseCase = getProjectLeadMonthEndStatusOverviewUseCase;
        this.prematureMonthEndPreparationUseCase = prematureMonthEndPreparationUseCase;
        this.createMonthEndClarificationUseCase = createMonthEndClarificationUseCase;
        this.completeMonthEndTaskUseCase = completeMonthEndTaskUseCase;
        this.updateMonthEndClarificationUseCase = updateMonthEndClarificationUseCase;
        this.completeMonthEndClarificationUseCase = completeMonthEndClarificationUseCase;
        this.deleteMonthEndClarificationUseCase = deleteMonthEndClarificationUseCase;
        this.generateMonthEndTasksUseCase = generateMonthEndTasksUseCase;
        this.projectSnapshotPort = projectSnapshotPort;
        this.userSnapshotPort = userSnapshotPort;
        this.authenticatedActorContext = authenticatedActorContext;
        this.transportHelper = transportHelper;
        this.monthEndRestMapper = monthEndRestMapper;
    }

    @Override
    @MegaRolesAllowed(Role.EMPLOYEE)
    public Response getEmployeeMonthEndStatusOverview(String month) {
        UserId actorId = authenticatedActorContext.userId();
        YearMonth parsedMonth = transportHelper.parseMonth(month);

        MonthEndStatusOverview overview = getEmployeeMonthEndStatusOverviewUseCase.getOverview(actorId, parsedMonth);

        return Response.ok(toOverviewResponse(overview, actorId)).build();
    }

    @Override
    @MegaRolesAllowed(Role.PROJECT_LEAD)
    public Response getProjectLeadMonthEndStatusOverview(String month) {
        UserId actorId = authenticatedActorContext.userId();
        YearMonth parsedMonth = transportHelper.parseMonth(month);

        MonthEndStatusOverview overview = getProjectLeadMonthEndStatusOverviewUseCase.getOverview(actorId, parsedMonth);

        return Response.ok(toOverviewResponse(overview, actorId)).build();
    }

    @Override
    @MegaRolesAllowed(Role.EMPLOYEE)
    public Response createMonthEndClarification(CreateClarificationRequestDto request) {
        UserId actorId = authenticatedActorContext.userId();
        UserId subjectEmployeeId = actorId;
        if (authenticatedActorContext.hasRole(Role.PROJECT_LEAD)) {
            if (request.getSubjectEmployeeId() != null) {
                subjectEmployeeId = transportHelper.toUserId(request.getSubjectEmployeeId());
            } else {
                subjectEmployeeId = null;
            }
        }

        MonthEndClarification clarification = createMonthEndClarificationUseCase.create(
                transportHelper.parseMonth(request.getMonth()),
                transportHelper.toProjectId(request.getProjectId()),
                subjectEmployeeId,
                actorId,
                request.getText()
        );

        Map<UserId, UserRef> userRefs = resolveUserRefs(clarification.referencedUserIds(), clarification.month());
        return Response.status(Response.Status.CREATED)
                .entity(monthEndRestMapper.toClarificationEntry(clarification, userRefs, actorId))
                .build();
    }

    @Override
    @MegaRolesAllowed(Role.EMPLOYEE)
    public Response prepareMonthEndProject(PrepareMonthEndProjectRequestDto request) {
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
        return Response.ok(monthEndRestMapper.toDto(result, userRefs, actorId)).build();
    }

    @Override
    @MegaRolesAllowed({Role.EMPLOYEE, Role.PROJECT_LEAD})
    public Response completeMonthEndTask(UUID taskId) {
        UserId actorId = authenticatedActorContext.userId();

        MonthEndTask task = completeMonthEndTaskUseCase.complete(
                transportHelper.toTaskId(taskId),
                actorId
        );

        return Response.ok(monthEndRestMapper.toDto(task)).build();
    }

    @Override
    @MegaRolesAllowed({Role.EMPLOYEE, Role.PROJECT_LEAD})
    public Response resolveMonthEndClarification(UUID clarificationId, ResolveClarificationRequestDto request) {
        UserId actorId = authenticatedActorContext.userId();

        MonthEndClarification clarification = completeMonthEndClarificationUseCase.complete(
                transportHelper.toClarificationId(clarificationId),
                actorId,
                request.getResolutionNote()
        );

        Map<UserId, UserRef> userRefs = resolveUserRefs(clarification.referencedUserIds(), clarification.month());
        return Response.ok(monthEndRestMapper.toClarificationEntry(clarification, userRefs, actorId)).build();
    }

    @Override
    @MegaRolesAllowed({Role.EMPLOYEE, Role.PROJECT_LEAD})
    public Response deleteMonthEndClarification(UUID clarificationId) {
        UserId actorId = authenticatedActorContext.userId();

        deleteMonthEndClarificationUseCase.delete(
                transportHelper.toClarificationId(clarificationId),
                actorId
        );

        return Response.noContent().build();
    }

    @Override
    @MegaRolesAllowed({Role.EMPLOYEE, Role.PROJECT_LEAD})
    public Response updateMonthEndClarificationText(UUID clarificationId, UpdateClarificationTextRequestDto request) {
        UserId actorId = authenticatedActorContext.userId();

        MonthEndClarification clarification = updateMonthEndClarificationUseCase.updateText(
                transportHelper.toClarificationId(clarificationId),
                actorId,
                request.getText()
        );

        Map<UserId, UserRef> userRefs = resolveUserRefs(clarification.referencedUserIds(), clarification.month());
        return Response.ok(monthEndRestMapper.toClarificationEntry(clarification, userRefs, actorId)).build();
    }

    @Override
    @Tenant("mega-cron")
    @RolesAllowed("mega-cron:sync")
    public Response generateMonthEndTasks(String month) {
        MonthEndTaskGenerationResult result = generateMonthEndTasksUseCase.generate(
                transportHelper.parseMonth(month)
        );

        return Response.ok(monthEndRestMapper.toDto(result)).build();
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

    private MonthEndStatusOverviewDto toOverviewResponse(
            MonthEndStatusOverview overview,
            UserId actorId
    ) {
        Map<ProjectId, ProjectRef> projectRefs = resolveProjectRefs(overview.tasks(), overview.month());
        Map<UserId, UserRef> userRefs = resolveUserRefs(overviewUserIds(overview), overview.month());
        return monthEndRestMapper.toDto(overview, projectRefs, userRefs, actorId);
    }

    private static Set<UserId> overviewUserIds(MonthEndStatusOverview overview) {
        return Stream.concat(
                overview.tasks().stream()
                        .map(MonthEndTask::subjectEmployeeId)
                        .filter(Objects::nonNull),
                overview.clarifications().stream()
                        .flatMap(clarification -> clarification.referencedUserIds().stream())
        ).collect(Collectors.toSet());
    }
}
