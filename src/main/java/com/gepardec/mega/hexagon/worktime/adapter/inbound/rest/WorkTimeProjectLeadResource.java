package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.application.interceptor.MegaRolesAllowed;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.hexagon.generated.api.WorkTimeProjectLeadApi;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import com.gepardec.mega.hexagon.worktime.domain.port.inbound.GetProjectLeadWorkTimeUseCase;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Authenticated
@MegaRolesAllowed(Role.PROJECT_LEAD)
public class WorkTimeProjectLeadResource implements WorkTimeProjectLeadApi {

    private final GetProjectLeadWorkTimeUseCase getProjectLeadWorkTimeUseCase;
    private final CurrentWorkTimeRestActorResolver currentWorkTimeRestActorResolver;
    private final WorkTimeRestTransportHelper workTimeRestTransportHelper;
    private final WorkTimeRestMapper workTimeRestMapper;

    @Inject
    public WorkTimeProjectLeadResource(
            GetProjectLeadWorkTimeUseCase getProjectLeadWorkTimeUseCase,
            CurrentWorkTimeRestActorResolver currentWorkTimeRestActorResolver,
            WorkTimeRestTransportHelper workTimeRestTransportHelper,
            WorkTimeRestMapper workTimeRestMapper
    ) {
        this.getProjectLeadWorkTimeUseCase = getProjectLeadWorkTimeUseCase;
        this.currentWorkTimeRestActorResolver = currentWorkTimeRestActorResolver;
        this.workTimeRestTransportHelper = workTimeRestTransportHelper;
        this.workTimeRestMapper = workTimeRestMapper;
    }

    @Override
    public Response getProjectLeadWorkTimeReport(String payrollMonth) {
        UserId actorId = currentWorkTimeRestActorResolver.resolveCurrentActorId();
        WorkTimeReport report = getProjectLeadWorkTimeUseCase.getWorkTime(
                actorId,
                workTimeRestTransportHelper.parsePayrollMonth(payrollMonth)
        );
        return Response.ok(workTimeRestMapper.toResponse(report)).build();
    }
}
