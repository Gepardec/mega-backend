package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.MonthEndSharedApi;
import com.gepardec.mega.hexagon.generated.model.ResolveClarificationRequest;
import com.gepardec.mega.hexagon.generated.model.UpdateClarificationTextRequest;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndTaskUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.DeleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.UpdateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.YearMonth;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequestScoped
@Authenticated
@MegaRolesAllowed({Role.EMPLOYEE, Role.PROJECT_LEAD})
public class MonthEndSharedResource implements MonthEndSharedApi {

    private final CompleteMonthEndTaskUseCase completeMonthEndTaskUseCase;
    private final UpdateMonthEndClarificationUseCase updateMonthEndClarificationUseCase;
    private final CompleteMonthEndClarificationUseCase completeMonthEndClarificationUseCase;
    private final DeleteMonthEndClarificationUseCase deleteMonthEndClarificationUseCase;
    private final MonthEndUserSnapshotPort userSnapshotPort;
    private final AuthenticatedActorContext authenticatedActorContext;
    private final MonthEndRestTransportHelper transportHelper;
    private final MonthEndRestMapper monthEndRestMapper;

    @Inject
    public MonthEndSharedResource(
            CompleteMonthEndTaskUseCase completeMonthEndTaskUseCase,
            UpdateMonthEndClarificationUseCase updateMonthEndClarificationUseCase,
            CompleteMonthEndClarificationUseCase completeMonthEndClarificationUseCase,
            DeleteMonthEndClarificationUseCase deleteMonthEndClarificationUseCase,
            MonthEndUserSnapshotPort userSnapshotPort,
            AuthenticatedActorContext authenticatedActorContext,
            MonthEndRestTransportHelper transportHelper,
            MonthEndRestMapper monthEndRestMapper
    ) {
        this.completeMonthEndTaskUseCase = completeMonthEndTaskUseCase;
        this.updateMonthEndClarificationUseCase = updateMonthEndClarificationUseCase;
        this.completeMonthEndClarificationUseCase = completeMonthEndClarificationUseCase;
        this.deleteMonthEndClarificationUseCase = deleteMonthEndClarificationUseCase;
        this.userSnapshotPort = userSnapshotPort;
        this.authenticatedActorContext = authenticatedActorContext;
        this.transportHelper = transportHelper;
        this.monthEndRestMapper = monthEndRestMapper;
    }

    @Override
    public Response completeMonthEndTask(UUID taskId) {
        UserId actorId = authenticatedActorContext.userId();
        MonthEndTask task = completeMonthEndTaskUseCase.complete(
                transportHelper.toTaskId(taskId),
                actorId
        );
        return Response.ok(monthEndRestMapper.toResponse(task)).build();
    }

    @Override
    public Response resolveMonthEndClarification(UUID clarificationId, ResolveClarificationRequest request) {
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
    public Response deleteMonthEndClarification(UUID clarificationId) {
        UserId actorId = authenticatedActorContext.userId();
        deleteMonthEndClarificationUseCase.delete(
                transportHelper.toClarificationId(clarificationId),
                actorId
        );
        return Response.noContent().build();
    }

    @Override
    public Response updateMonthEndClarificationText(UUID clarificationId, UpdateClarificationTextRequest request) {
        UserId actorId = authenticatedActorContext.userId();
        MonthEndClarification clarification = updateMonthEndClarificationUseCase.updateText(
                transportHelper.toClarificationId(clarificationId),
                actorId,
                request.getText()
        );
        Map<UserId, UserRef> userRefs = resolveUserRefs(clarification.referencedUserIds(), clarification.month());
        return Response.ok(monthEndRestMapper.toClarificationEntry(clarification, userRefs, actorId)).build();
    }

    private Map<UserId, UserRef> resolveUserRefs(Set<UserId> ids, YearMonth month) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userSnapshotPort.findByIds(ids, month).stream()
                .collect(Collectors.toMap(UserRef::id, Function.identity()));
    }
}
