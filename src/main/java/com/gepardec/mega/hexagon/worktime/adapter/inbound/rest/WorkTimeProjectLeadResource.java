package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.WorkTimeProjectLeadApi;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetProjectLeadWorkTimeUseCase;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Authenticated
@MegaRolesAllowed(Role.PROJECT_LEAD)
public class WorkTimeProjectLeadResource implements WorkTimeProjectLeadApi {

    private final GetProjectLeadWorkTimeUseCase getProjectLeadWorkTimeUseCase;
    private final AuthenticatedActorContext authenticatedActorContext;
    private final WorkTimeRestTransportHelper workTimeRestTransportHelper;
    private final WorkTimeRestMapper workTimeRestMapper;

    @Inject
    public WorkTimeProjectLeadResource(
            GetProjectLeadWorkTimeUseCase getProjectLeadWorkTimeUseCase,
            AuthenticatedActorContext authenticatedActorContext,
            WorkTimeRestTransportHelper workTimeRestTransportHelper,
            WorkTimeRestMapper workTimeRestMapper
    ) {
        this.getProjectLeadWorkTimeUseCase = getProjectLeadWorkTimeUseCase;
        this.authenticatedActorContext = authenticatedActorContext;
        this.workTimeRestTransportHelper = workTimeRestTransportHelper;
        this.workTimeRestMapper = workTimeRestMapper;
    }

    @Override
    public Response getProjectLeadWorkTimeReport(String payrollMonth) {
        UserId actorId = authenticatedActorContext.userId();
        WorkTimeReport report = getProjectLeadWorkTimeUseCase.getWorkTime(
                actorId,
                workTimeRestTransportHelper.parsePayrollMonth(payrollMonth)
        );
        return Response.ok(workTimeRestMapper.toResponse(report)).build();
    }
}
