package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.application.interceptor.MegaRolesAllowed;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.hexagon.generated.api.MonthEndSharedApi;
import com.gepardec.mega.hexagon.generated.model.ResolveClarificationRequest;
import com.gepardec.mega.hexagon.generated.model.UpdateClarificationTextRequest;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.CompleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.CompleteMonthEndTaskUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.UpdateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
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
    private final CurrentMonthEndRestActorResolver currentMonthEndRestActorResolver;
    private final MonthEndRestTransportHelper transportHelper;
    private final MonthEndRestMapper monthEndRestMapper;

    @Inject
    public MonthEndSharedResource(
            GetMonthEndStatusOverviewUseCase getMonthEndStatusOverviewUseCase,
            CompleteMonthEndTaskUseCase completeMonthEndTaskUseCase,
            UpdateMonthEndClarificationUseCase updateMonthEndClarificationUseCase,
            CompleteMonthEndClarificationUseCase completeMonthEndClarificationUseCase,
            CurrentMonthEndRestActorResolver currentMonthEndRestActorResolver,
            MonthEndRestTransportHelper transportHelper,
            MonthEndRestMapper monthEndRestMapper
    ) {
        this.getMonthEndStatusOverviewUseCase = getMonthEndStatusOverviewUseCase;
        this.completeMonthEndTaskUseCase = completeMonthEndTaskUseCase;
        this.updateMonthEndClarificationUseCase = updateMonthEndClarificationUseCase;
        this.completeMonthEndClarificationUseCase = completeMonthEndClarificationUseCase;
        this.currentMonthEndRestActorResolver = currentMonthEndRestActorResolver;
        this.transportHelper = transportHelper;
        this.monthEndRestMapper = monthEndRestMapper;
    }

    @Override
    public Response completeMonthEndTask(UUID taskId) {
        UserId actorId = currentMonthEndRestActorResolver.resolveCurrentActorId();
        MonthEndTask task = completeMonthEndTaskUseCase.complete(
                transportHelper.toTaskId(taskId),
                actorId
        );
        return Response.ok(monthEndRestMapper.toResponse(task)).build();
    }

    @Override
    public Response getMonthEndStatusOverview(String month) {
        UserId actorId = currentMonthEndRestActorResolver.resolveCurrentActorId();
        MonthEndStatusOverview overview = getMonthEndStatusOverviewUseCase.getOverview(
                actorId,
                transportHelper.parseMonth(month)
        );
        return Response.ok(monthEndRestMapper.toResponse(overview)).build();
    }

    @Override
    public Response resolveMonthEndClarification(UUID clarificationId, ResolveClarificationRequest request) {
        UserId actorId = currentMonthEndRestActorResolver.resolveCurrentActorId();
        MonthEndClarification clarification = completeMonthEndClarificationUseCase.complete(
                transportHelper.toClarificationId(clarificationId),
                actorId,
                request.getResolutionNote()
        );
        return Response.ok(monthEndRestMapper.toResponse(clarification)).build();
    }

    @Override
    public Response updateMonthEndClarificationText(UUID clarificationId, UpdateClarificationTextRequest request) {
        UserId actorId = currentMonthEndRestActorResolver.resolveCurrentActorId();
        MonthEndClarification clarification = updateMonthEndClarificationUseCase.updateText(
                transportHelper.toClarificationId(clarificationId),
                actorId,
                request.getText()
        );
        return Response.ok(monthEndRestMapper.toResponse(clarification)).build();
    }
}
