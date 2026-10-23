package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.MonthEndApi;
import com.gepardec.mega.hexagon.generated.model.CompleteProjectLeadMonthEndTasksRequestDto;
import com.gepardec.mega.hexagon.generated.model.CreateClarificationRequestDto;
import com.gepardec.mega.hexagon.generated.model.GenerateMonthEndPrematurelyRequestDto;
import com.gepardec.mega.hexagon.generated.model.MonthEndStatusOverviewDto;
import com.gepardec.mega.hexagon.generated.model.MonthEndTaskCompletionDto;
import com.gepardec.mega.hexagon.generated.model.MonthEndTaskDto;
import com.gepardec.mega.hexagon.generated.model.ResolveClarificationRequestDto;
import com.gepardec.mega.hexagon.generated.model.UpdateClarificationTextRequestDto;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteEmployeeMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndTaskUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteProjectLeadMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.DeleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetEmployeeMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetEmployeePayrollMonthUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetProjectLeadMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetProjectLeadPayrollMonthUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.PrematureMonthEndPreparationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.UpdateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.application.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
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
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequestScoped
@Authenticated
public class MonthEndResource implements MonthEndApi {

    private final GetEmployeePayrollMonthUseCase getEmployeePayrollMonthUseCase;
    private final GetProjectLeadPayrollMonthUseCase getProjectLeadPayrollMonthUseCase;
    private final GetEmployeeMonthEndStatusOverviewUseCase getEmployeeMonthEndStatusOverviewUseCase;
    private final GetProjectLeadMonthEndStatusOverviewUseCase getProjectLeadMonthEndStatusOverviewUseCase;
    private final PrematureMonthEndPreparationUseCase prematureMonthEndPreparationUseCase;
    private final CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase;
    private final CompleteMonthEndTaskUseCase completeMonthEndTaskUseCase;
    private final CompleteProjectLeadMonthEndTasksUseCase completeProjectLeadMonthEndTasksUseCase;
    private final CompleteEmployeeMonthEndTasksUseCase completeEmployeeMonthEndTasksUseCase;
    private final UpdateMonthEndClarificationUseCase updateMonthEndClarificationUseCase;
    private final CompleteMonthEndClarificationUseCase completeMonthEndClarificationUseCase;
    private final DeleteMonthEndClarificationUseCase deleteMonthEndClarificationUseCase;
    private final MonthEndProjectSnapshotPort projectSnapshotPort;
    private final MonthEndUserSnapshotPort userSnapshotPort;
    private final AuthenticatedActorContext authenticatedActorContext;
    private final MonthEndRestMapper monthEndRestMapper;

    @Inject
    public MonthEndResource(
            GetEmployeePayrollMonthUseCase getEmployeePayrollMonthUseCase,
            GetProjectLeadPayrollMonthUseCase getProjectLeadPayrollMonthUseCase,
            GetEmployeeMonthEndStatusOverviewUseCase getEmployeeMonthEndStatusOverviewUseCase,
            GetProjectLeadMonthEndStatusOverviewUseCase getProjectLeadMonthEndStatusOverviewUseCase,
            PrematureMonthEndPreparationUseCase prematureMonthEndPreparationUseCase,
            CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase,
            CompleteMonthEndTaskUseCase completeMonthEndTaskUseCase,
            CompleteProjectLeadMonthEndTasksUseCase completeProjectLeadMonthEndTasksUseCase,
            CompleteEmployeeMonthEndTasksUseCase completeEmployeeMonthEndTasksUseCase,
            UpdateMonthEndClarificationUseCase updateMonthEndClarificationUseCase,
            CompleteMonthEndClarificationUseCase completeMonthEndClarificationUseCase,
            DeleteMonthEndClarificationUseCase deleteMonthEndClarificationUseCase,
            MonthEndProjectSnapshotPort projectSnapshotPort,
            MonthEndUserSnapshotPort userSnapshotPort,
            AuthenticatedActorContext authenticatedActorContext,
            MonthEndRestMapper monthEndRestMapper
    ) {
        this.getEmployeePayrollMonthUseCase = getEmployeePayrollMonthUseCase;
        this.getProjectLeadPayrollMonthUseCase = getProjectLeadPayrollMonthUseCase;
        this.getEmployeeMonthEndStatusOverviewUseCase = getEmployeeMonthEndStatusOverviewUseCase;
        this.getProjectLeadMonthEndStatusOverviewUseCase = getProjectLeadMonthEndStatusOverviewUseCase;
        this.prematureMonthEndPreparationUseCase = prematureMonthEndPreparationUseCase;
        this.createMonthEndClarificationUseCase = createMonthEndClarificationUseCase;
        this.completeMonthEndTaskUseCase = completeMonthEndTaskUseCase;
        this.completeProjectLeadMonthEndTasksUseCase = completeProjectLeadMonthEndTasksUseCase;
        this.completeEmployeeMonthEndTasksUseCase = completeEmployeeMonthEndTasksUseCase;
        this.updateMonthEndClarificationUseCase = updateMonthEndClarificationUseCase;
        this.completeMonthEndClarificationUseCase = completeMonthEndClarificationUseCase;
        this.deleteMonthEndClarificationUseCase = deleteMonthEndClarificationUseCase;
        this.projectSnapshotPort = projectSnapshotPort;
        this.userSnapshotPort = userSnapshotPort;
        this.authenticatedActorContext = authenticatedActorContext;
        this.monthEndRestMapper = monthEndRestMapper;
    }

    @Override
    @MegaRolesAllowed(Role.EMPLOYEE)
    public Response getEmployeePayrollMonth() {
        UserId actorId = authenticatedActorContext.userId();
        YearMonth payrollMonth = getEmployeePayrollMonthUseCase.getPayrollMonth(actorId);

        return Response.ok(payrollMonth).build();
    }

    @Override
    @MegaRolesAllowed(Role.PROJECT_LEAD)
    public Response getProjectLeadPayrollMonth() {
        YearMonth payrollMonth = getProjectLeadPayrollMonthUseCase.getPayrollMonth(authenticatedActorContext.userId());

        return Response.ok(payrollMonth).build();
    }

    @Override
    @MegaRolesAllowed(Role.EMPLOYEE)
    public Response getEmployeeMonthEndStatusOverview(YearMonth month) {
        UserId actorId = authenticatedActorContext.userId();
        MonthEndStatusOverview overview = getEmployeeMonthEndStatusOverviewUseCase.getOverview(actorId, month);

        return Response.ok(toOverviewResponse(overview, actorId)).build();
    }

    @Override
    @MegaRolesAllowed(Role.PROJECT_LEAD)
    public Response getProjectLeadMonthEndStatusOverview(YearMonth month) {
        UserId actorId = authenticatedActorContext.userId();
        MonthEndStatusOverview overview = getProjectLeadMonthEndStatusOverviewUseCase.getOverview(actorId, month);

        return Response.ok(toOverviewResponse(overview, actorId)).build();
    }

    @Override
    @MegaRolesAllowed(Role.EMPLOYEE)
    public Response createMonthEndClarification(CreateClarificationRequestDto createClarificationRequestDto) {
        UserId actorId = authenticatedActorContext.userId();
        UserId subjectEmployeeId = actorId;
        if (authenticatedActorContext.hasRole(Role.PROJECT_LEAD)) {
            if (createClarificationRequestDto.getSubjectEmployeeId() != null) {
                subjectEmployeeId = UserId.of(createClarificationRequestDto.getSubjectEmployeeId());
            } else {
                subjectEmployeeId = null;
            }
        }

        MonthEndClarification clarification = createMonthEndClarificationUseCase.create(
                createClarificationRequestDto.getMonth(),
                ProjectId.of(createClarificationRequestDto.getProjectId()),
                subjectEmployeeId,
                actorId,
                createClarificationRequestDto.getText()
        );

        Map<UserId, UserRef> userRefs = resolveUserRefs(clarification.referencedUserIds(), clarification.month());
        return Response.status(Response.Status.CREATED)
                .entity(monthEndRestMapper.toClarificationEntry(clarification, userRefs, actorId))
                .build();
    }

    @Override
    @MegaRolesAllowed(Role.EMPLOYEE)
    public Response generateMonthEndPrematurely(GenerateMonthEndPrematurelyRequestDto generateMonthEndPrematurelyRequestDto) {
        UserId actorId = authenticatedActorContext.userId();

        prematureMonthEndPreparationUseCase.prepare(
                generateMonthEndPrematurelyRequestDto.getMonth(),
                actorId,
                generateMonthEndPrematurelyRequestDto.getClarificationText()
        );

        return Response.noContent().build();
    }

    @Override
    @MegaRolesAllowed({Role.EMPLOYEE, Role.PROJECT_LEAD})
    public Response completeMonthEndTask(UUID taskId) {
        UserId actorId = authenticatedActorContext.userId();

        MonthEndTask task = completeMonthEndTaskUseCase.complete(
                MonthEndTaskId.of(taskId),
                actorId
        );

        return Response.ok(monthEndRestMapper.toDto(task)).build();
    }

    @Override
    @MegaRolesAllowed(Role.PROJECT_LEAD)
    public Response completeProjectLeadMonthEndTasks(YearMonth month, CompleteProjectLeadMonthEndTasksRequestDto completeProjectLeadMonthEndTasksRequestDto) {
        UserId actorId = authenticatedActorContext.userId();

        List<MonthEndTask> tasks = completeProjectLeadMonthEndTasksUseCase.complete(
                month,
                ProjectId.of(completeProjectLeadMonthEndTasksRequestDto.getProjectId()),
                monthEndRestMapper.toDomain(completeProjectLeadMonthEndTasksRequestDto.getType()),
                actorId
        );

        return Response.ok(toCompletionResponse(tasks)).build();
    }

    @Override
    @MegaRolesAllowed(Role.EMPLOYEE)
    public Response completeEmployeeMonthEndTasks(YearMonth month) {
        UserId actorId = authenticatedActorContext.userId();

        List<MonthEndTask> tasks = completeEmployeeMonthEndTasksUseCase.complete(
                month,
                actorId
        );

        return Response.ok(toCompletionResponse(tasks)).build();
    }

    @Override
    @MegaRolesAllowed({Role.EMPLOYEE, Role.PROJECT_LEAD})
    public Response resolveMonthEndClarification(UUID clarificationId, ResolveClarificationRequestDto resolveClarificationRequestDto) {
        UserId actorId = authenticatedActorContext.userId();

        MonthEndClarification clarification = completeMonthEndClarificationUseCase.complete(
                MonthEndClarificationId.of(clarificationId),
                actorId,
                resolveClarificationRequestDto.getResolutionNote()
        );

        Map<UserId, UserRef> userRefs = resolveUserRefs(clarification.referencedUserIds(), clarification.month());
        return Response.ok(monthEndRestMapper.toClarificationEntry(clarification, userRefs, actorId)).build();
    }

    @Override
    @MegaRolesAllowed({Role.EMPLOYEE, Role.PROJECT_LEAD})
    public Response deleteMonthEndClarification(UUID clarificationId) {
        UserId actorId = authenticatedActorContext.userId();

        deleteMonthEndClarificationUseCase.delete(
                MonthEndClarificationId.of(clarificationId),
                actorId
        );

        return Response.noContent().build();
    }

    @Override
    @MegaRolesAllowed({Role.EMPLOYEE, Role.PROJECT_LEAD})
    public Response updateMonthEndClarificationText(UUID clarificationId, UpdateClarificationTextRequestDto updateClarificationTextRequestDto) {
        UserId actorId = authenticatedActorContext.userId();

        MonthEndClarification clarification = updateMonthEndClarificationUseCase.updateText(
                MonthEndClarificationId.of(clarificationId),
                actorId,
                updateClarificationTextRequestDto.getText()
        );

        Map<UserId, UserRef> userRefs = resolveUserRefs(clarification.referencedUserIds(), clarification.month());
        return Response.ok(monthEndRestMapper.toClarificationEntry(clarification, userRefs, actorId)).build();
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

    private MonthEndTaskCompletionDto toCompletionResponse(List<MonthEndTask> tasks) {
        List<MonthEndTaskDto> taskDtos = tasks.stream()
                .map(monthEndRestMapper::toDto)
                .toList();
        return new MonthEndTaskCompletionDto().completed(taskDtos);
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
