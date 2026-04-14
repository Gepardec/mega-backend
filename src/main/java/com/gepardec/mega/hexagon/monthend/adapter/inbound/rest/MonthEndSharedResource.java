package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.MonthEndSharedApi;
import com.gepardec.mega.hexagon.generated.model.ResolveClarificationRequest;
import com.gepardec.mega.hexagon.generated.model.UpdateClarificationTextRequest;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndTaskUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.UpdateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@RequestScoped
@Authenticated
@MegaRolesAllowed({Role.EMPLOYEE, Role.PROJECT_LEAD})
public class MonthEndSharedResource implements MonthEndSharedApi {

    private final GetMonthEndStatusOverviewUseCase getMonthEndStatusOverviewUseCase;
    private final CompleteMonthEndTaskUseCase completeMonthEndTaskUseCase;
    private final UpdateMonthEndClarificationUseCase updateMonthEndClarificationUseCase;
    private final CompleteMonthEndClarificationUseCase completeMonthEndClarificationUseCase;
    private final AuthenticatedActorContext authenticatedActorContext;
    private final MonthEndRestTransportHelper transportHelper;
    private final MonthEndRestMapper monthEndRestMapper;

    @Inject
    public MonthEndSharedResource(
            GetMonthEndStatusOverviewUseCase getMonthEndStatusOverviewUseCase,
            CompleteMonthEndTaskUseCase completeMonthEndTaskUseCase,
            UpdateMonthEndClarificationUseCase updateMonthEndClarificationUseCase,
            CompleteMonthEndClarificationUseCase completeMonthEndClarificationUseCase,
            AuthenticatedActorContext authenticatedActorContext,
            MonthEndRestTransportHelper transportHelper,
            MonthEndRestMapper monthEndRestMapper
    ) {
        this.getMonthEndStatusOverviewUseCase = getMonthEndStatusOverviewUseCase;
        this.completeMonthEndTaskUseCase = completeMonthEndTaskUseCase;
        this.updateMonthEndClarificationUseCase = updateMonthEndClarificationUseCase;
        this.completeMonthEndClarificationUseCase = completeMonthEndClarificationUseCase;
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
    public Response getMonthEndStatusOverview(String month) {
        UserId actorId = authenticatedActorContext.userId();
        MonthEndStatusOverview overview = getMonthEndStatusOverviewUseCase.getOverview(
                actorId,
                transportHelper.parseMonth(month)
        );
        return Response.ok(monthEndRestMapper.toResponse(overview)).build();
    }

    @Override
    public Response resolveMonthEndClarification(UUID clarificationId, ResolveClarificationRequest request) {
        UserId actorId = authenticatedActorContext.userId();
        MonthEndClarification clarification = completeMonthEndClarificationUseCase.complete(
                transportHelper.toClarificationId(clarificationId),
                actorId,
                request.getResolutionNote()
        );
        return Response.ok(monthEndRestMapper.toResponse(clarification)).build();
    }

    @Override
    public Response updateMonthEndClarificationText(UUID clarificationId, UpdateClarificationTextRequest request) {
        UserId actorId = authenticatedActorContext.userId();
        MonthEndClarification clarification = updateMonthEndClarificationUseCase.updateText(
                transportHelper.toClarificationId(clarificationId),
                actorId,
                request.getText()
        );
        return Response.ok(monthEndRestMapper.toResponse(clarification)).build();
    }
}
