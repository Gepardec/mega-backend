package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.MonthEndProjectLeadApi;
import com.gepardec.mega.hexagon.generated.model.CreateProjectLeadClarificationRequest;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetProjectLeadMonthEndWorklistUseCase;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
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
    private final AuthenticatedActorContext authenticatedActorContext;
    private final MonthEndRestTransportHelper transportHelper;
    private final MonthEndRestMapper monthEndRestMapper;

    @Inject
    public MonthEndProjectLeadResource(
            GetProjectLeadMonthEndWorklistUseCase getProjectLeadMonthEndWorklistUseCase,
            CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase,
            AuthenticatedActorContext authenticatedActorContext,
            MonthEndRestTransportHelper transportHelper,
            MonthEndRestMapper monthEndRestMapper
    ) {
        this.getProjectLeadMonthEndWorklistUseCase = getProjectLeadMonthEndWorklistUseCase;
        this.createMonthEndClarificationUseCase = createMonthEndClarificationUseCase;
        this.authenticatedActorContext = authenticatedActorContext;
        this.transportHelper = transportHelper;
        this.monthEndRestMapper = monthEndRestMapper;
    }

    @Override
    public Response createProjectLeadMonthEndClarification(CreateProjectLeadClarificationRequest request) {
        UserId actorId = authenticatedActorContext.userId();
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
        UserId actorId = authenticatedActorContext.userId();
        MonthEndWorklist worklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(
                actorId,
                transportHelper.parseMonth(month)
        );
        return Response.ok(monthEndRestMapper.toResponse(worklist)).build();
    }
}
