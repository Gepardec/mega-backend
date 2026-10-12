package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.WorkTimeEmployeeApi;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetEmployeeWorkTimeUseCase;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetEmployeeWarningsUseCase;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.util.List;

@RequestScoped
@Authenticated
@MegaRolesAllowed(Role.EMPLOYEE)
public class WorkTimeEmployeeResource implements WorkTimeEmployeeApi {

    private final GetEmployeeWorkTimeUseCase getEmployeeWorkTimeUseCase;
    private final AuthenticatedActorContext authenticatedActorContext;
    private final WorkTimeRestTransportHelper workTimeRestTransportHelper;
    private final WorkTimeRestMapper workTimeRestMapper;
    private final GetEmployeeWarningsUseCase getEmployeeWarningsUseCase;
    private final WorkTimeWarningRestMapper workTimeWarningRestMapper;

    @Inject
    public WorkTimeEmployeeResource(
            GetEmployeeWorkTimeUseCase getEmployeeWorkTimeUseCase,
            AuthenticatedActorContext authenticatedActorContext,
            WorkTimeRestTransportHelper workTimeRestTransportHelper,
            WorkTimeRestMapper workTimeRestMapper,
            GetEmployeeWarningsUseCase getEmployeeWarningsUseCase,
            WorkTimeWarningRestMapper workTimeWarningRestMapper
    ) {
        this.getEmployeeWorkTimeUseCase = getEmployeeWorkTimeUseCase;
        this.authenticatedActorContext = authenticatedActorContext;
        this.workTimeRestTransportHelper = workTimeRestTransportHelper;
        this.workTimeRestMapper = workTimeRestMapper;
        this.getEmployeeWarningsUseCase = getEmployeeWarningsUseCase;
        this.workTimeWarningRestMapper = workTimeWarningRestMapper;
    }

    @Override
    public Response getEmployeeWorkTimeReport(String payrollMonth) {
        UserId actorId = authenticatedActorContext.userId();
        WorkTimeReport report = getEmployeeWorkTimeUseCase.getWorkTime(
                actorId,
                workTimeRestTransportHelper.parsePayrollMonth(payrollMonth)
        );
        return Response.ok(workTimeRestMapper.toDto(report)).build();
    }

    @Override
    public Response getEmployeeWarnings(String payrollMonth) {
        UserId actorId = authenticatedActorContext.userId();
        List<WorkTimeWarning> warnings = getEmployeeWarningsUseCase.getWarnings(
                actorId,
                workTimeRestTransportHelper.parsePayrollMonth(payrollMonth)
        );
        return Response.ok(workTimeWarningRestMapper.toDto(warnings)).build();
    }
}
