package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.MonthEndEmployeeApi;
import com.gepardec.mega.hexagon.generated.model.CreateEmployeeClarificationRequest;
import com.gepardec.mega.hexagon.generated.model.PrepareMonthEndProjectRequest;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetEmployeeMonthEndWorklistUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.PrematureMonthEndPreparationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndPreparationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Authenticated
@MegaRolesAllowed(Role.EMPLOYEE)
public class MonthEndEmployeeResource implements MonthEndEmployeeApi {

    private final GetEmployeeMonthEndWorklistUseCase getEmployeeMonthEndWorklistUseCase;
    private final PrematureMonthEndPreparationUseCase prematureMonthEndPreparationUseCase;
    private final CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase;
    private final AuthenticatedActorContext authenticatedActorContext;
    private final MonthEndRestTransportHelper transportHelper;
    private final MonthEndRestMapper monthEndRestMapper;

    @Inject
    public MonthEndEmployeeResource(
            GetEmployeeMonthEndWorklistUseCase getEmployeeMonthEndWorklistUseCase,
            PrematureMonthEndPreparationUseCase prematureMonthEndPreparationUseCase,
            CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase,
            AuthenticatedActorContext authenticatedActorContext,
            MonthEndRestTransportHelper transportHelper,
            MonthEndRestMapper monthEndRestMapper
    ) {
        this.getEmployeeMonthEndWorklistUseCase = getEmployeeMonthEndWorklistUseCase;
        this.prematureMonthEndPreparationUseCase = prematureMonthEndPreparationUseCase;
        this.createMonthEndClarificationUseCase = createMonthEndClarificationUseCase;
        this.authenticatedActorContext = authenticatedActorContext;
        this.transportHelper = transportHelper;
        this.monthEndRestMapper = monthEndRestMapper;
    }

    @Override
    public Response createEmployeeMonthEndClarification(CreateEmployeeClarificationRequest request) {
        UserId actorId = authenticatedActorContext.userId();
        MonthEndClarification clarification = createMonthEndClarificationUseCase.create(
                transportHelper.parseMonth(request.getMonth()),
                transportHelper.toProjectId(request.getProjectId()),
                actorId,
                actorId,
                MonthEndClarificationSide.EMPLOYEE,
                request.getText()
        );
        return Response.status(Response.Status.CREATED)
                .entity(monthEndRestMapper.toResponse(clarification))
                .build();
    }

    @Override
    public Response getEmployeeMonthEndWorklist(String month) {
        UserId actorId = authenticatedActorContext.userId();
        MonthEndWorklist worklist = getEmployeeMonthEndWorklistUseCase.getWorklist(
                actorId,
                transportHelper.parseMonth(month)
        );
        return Response.ok(monthEndRestMapper.toResponse(worklist)).build();
    }

    @Override
    public Response prepareEmployeeMonthEndProject(PrepareMonthEndProjectRequest request) {
        UserId actorId = authenticatedActorContext.userId();
        MonthEndPreparationResult result = prematureMonthEndPreparationUseCase.prepare(
                transportHelper.parseMonth(request.getMonth()),
                transportHelper.toProjectId(request.getProjectId()),
                actorId,
                request.getClarificationText()
        );
        return Response.ok(monthEndRestMapper.toResponse(result)).build();
    }
}
