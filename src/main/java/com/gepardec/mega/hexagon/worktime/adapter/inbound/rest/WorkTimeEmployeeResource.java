package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.application.interceptor.MegaRolesAllowed;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.hexagon.generated.api.WorkTimeEmployeeApi;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import com.gepardec.mega.hexagon.worktime.domain.port.inbound.GetEmployeeWorkTimeUseCase;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Authenticated
@MegaRolesAllowed(Role.EMPLOYEE)
public class WorkTimeEmployeeResource implements WorkTimeEmployeeApi {

    private final GetEmployeeWorkTimeUseCase getEmployeeWorkTimeUseCase;
    private final CurrentWorkTimeRestActorResolver currentWorkTimeRestActorResolver;
    private final WorkTimeRestTransportHelper workTimeRestTransportHelper;
    private final WorkTimeRestMapper workTimeRestMapper;

    @Inject
    public WorkTimeEmployeeResource(
            GetEmployeeWorkTimeUseCase getEmployeeWorkTimeUseCase,
            CurrentWorkTimeRestActorResolver currentWorkTimeRestActorResolver,
            WorkTimeRestTransportHelper workTimeRestTransportHelper,
            WorkTimeRestMapper workTimeRestMapper
    ) {
        this.getEmployeeWorkTimeUseCase = getEmployeeWorkTimeUseCase;
        this.currentWorkTimeRestActorResolver = currentWorkTimeRestActorResolver;
        this.workTimeRestTransportHelper = workTimeRestTransportHelper;
        this.workTimeRestMapper = workTimeRestMapper;
    }

    @Override
    public Response getEmployeeWorkTimeReport(String payrollMonth) {
        UserId actorId = currentWorkTimeRestActorResolver.resolveCurrentActorId();
        WorkTimeReport report = getEmployeeWorkTimeUseCase.getWorkTime(
                actorId,
                workTimeRestTransportHelper.parsePayrollMonth(payrollMonth)
        );
        return Response.ok(workTimeRestMapper.toResponse(report)).build();
    }
}
