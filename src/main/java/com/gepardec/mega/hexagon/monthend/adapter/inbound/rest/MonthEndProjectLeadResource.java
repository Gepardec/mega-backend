package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.application.interceptor.MegaRolesAllowed;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.hexagon.generated.api.MonthEndProjectLeadApi;
import com.gepardec.mega.hexagon.generated.model.CreateProjectLeadClarificationRequest;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetProjectLeadMonthEndWorklistUseCase;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Authenticated
@MegaRolesAllowed(Role.PROJECT_LEAD)
public class MonthEndProjectLeadResource implements MonthEndProjectLeadApi {

    private final GetProjectLeadMonthEndWorklistUseCase getProjectLeadMonthEndWorklistUseCase;
    private final CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase;
    private final CurrentMonthEndRestActorResolver currentMonthEndRestActorResolver;
    private final MonthEndRestTransportHelper transportHelper;
    private final MonthEndRestMapper monthEndRestMapper;

    @Inject
    public MonthEndProjectLeadResource(
            GetProjectLeadMonthEndWorklistUseCase getProjectLeadMonthEndWorklistUseCase,
            CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase,
            CurrentMonthEndRestActorResolver currentMonthEndRestActorResolver,
            MonthEndRestTransportHelper transportHelper,
            MonthEndRestMapper monthEndRestMapper
    ) {
        this.getProjectLeadMonthEndWorklistUseCase = getProjectLeadMonthEndWorklistUseCase;
        this.createMonthEndClarificationUseCase = createMonthEndClarificationUseCase;
        this.currentMonthEndRestActorResolver = currentMonthEndRestActorResolver;
        this.transportHelper = transportHelper;
        this.monthEndRestMapper = monthEndRestMapper;
    }

    @Override
    public Response createProjectLeadMonthEndClarification(CreateProjectLeadClarificationRequest request) {
        UserId actorId = currentMonthEndRestActorResolver.resolveCurrentActorId();
        MonthEndClarification clarification = createMonthEndClarificationUseCase.create(
                transportHelper.parseMonth(request.getMonth()),
                transportHelper.toProjectId(request.getProjectId()),
                transportHelper.toUserId(request.getSubjectEmployeeId()),
                actorId,
                MonthEndClarificationSide.PROJECT_LEAD,
                request.getText()
        );
        return Response.status(Response.Status.CREATED)
                .entity(monthEndRestMapper.toResponse(clarification))
                .build();
    }

    @Override
    public Response getProjectLeadMonthEndWorklist(String month) {
        UserId actorId = currentMonthEndRestActorResolver.resolveCurrentActorId();
        MonthEndWorklist worklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(
                actorId,
                transportHelper.parseMonth(month)
        );
        return Response.ok(monthEndRestMapper.toResponse(worklist)).build();
    }
}
