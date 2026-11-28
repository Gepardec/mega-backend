package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.WorkTimeApi;
import com.gepardec.mega.hexagon.generated.model.CapacityDto;
import com.gepardec.mega.hexagon.shared.adapter.inbound.rest.SharedRefRestMapper;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.Capacity;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetEmployeeWarningsUseCase;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetWorkTimeReportUseCase;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.YearMonth;
import java.util.List;

@RequestScoped
@Authenticated
public class WorkTimeResource implements WorkTimeApi {

    private final GetWorkTimeReportUseCase getWorkTimeReportUseCase;
    private final AuthenticatedActorContext authenticatedActorContext;
    private final WorkTimeRestMapper workTimeRestMapper;
    private final GetEmployeeWarningsUseCase getEmployeeWarningsUseCase;
    private final WorkTimeWarningRestMapper workTimeWarningRestMapper;
    private final SharedRefRestMapper sharedRefRestMapper;

    @Inject
    public WorkTimeResource(
            GetWorkTimeReportUseCase getWorkTimeReportUseCase,
            AuthenticatedActorContext authenticatedActorContext,
            WorkTimeRestMapper workTimeRestMapper,
            GetEmployeeWarningsUseCase getEmployeeWarningsUseCase,
            WorkTimeWarningRestMapper workTimeWarningRestMapper,
            SharedRefRestMapper sharedRefRestMapper
    ) {
        this.getWorkTimeReportUseCase = getWorkTimeReportUseCase;
        this.authenticatedActorContext = authenticatedActorContext;
        this.workTimeRestMapper = workTimeRestMapper;
        this.getEmployeeWarningsUseCase = getEmployeeWarningsUseCase;
        this.workTimeWarningRestMapper = workTimeWarningRestMapper;
        this.sharedRefRestMapper = sharedRefRestMapper;
    }

    @Override
    @MegaRolesAllowed(Role.EMPLOYEE)
    public Response getWorkTimeReport(YearMonth payrollMonth, CapacityDto capacity) {
        Capacity domainCapacity = sharedRefRestMapper.toDomain(capacity);
        authenticatedActorContext.requireCapacity(domainCapacity);

        UserId actorId = authenticatedActorContext.userId();
        WorkTimeReport report = getWorkTimeReportUseCase.getWorkTime(
                actorId,
                payrollMonth,
                domainCapacity
        );
        return Response.ok(workTimeRestMapper.toDto(report)).build();
    }

    @Override
    @MegaRolesAllowed(Role.EMPLOYEE)
    public Response getEmployeeWarnings(YearMonth payrollMonth) {
        UserId actorId = authenticatedActorContext.userId();
        List<WorkTimeWarning> warnings = getEmployeeWarningsUseCase.getWarnings(
                actorId,
                payrollMonth
        );
        return Response.ok(workTimeWarningRestMapper.toDto(warnings)).build();
    }
}
