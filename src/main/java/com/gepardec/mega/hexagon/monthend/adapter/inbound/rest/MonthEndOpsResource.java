package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.MonthEndOpsApi;
import com.gepardec.mega.hexagon.generated.model.GenerateMonthEndTasksRequest;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GenerateMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import io.quarkus.oidc.Tenant;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Tenant("mega-cron")
@RolesAllowed("mega-cron:sync")
public class MonthEndOpsResource implements MonthEndOpsApi {

    private final GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase;
    private final MonthEndRestTransportHelper transportHelper;
    private final MonthEndRestMapper monthEndRestMapper;

    @Inject
    public MonthEndOpsResource(
            GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase,
            MonthEndRestTransportHelper transportHelper,
            MonthEndRestMapper monthEndRestMapper
    ) {
        this.generateMonthEndTasksUseCase = generateMonthEndTasksUseCase;
        this.transportHelper = transportHelper;
        this.monthEndRestMapper = monthEndRestMapper;
    }

    @Override
    public Response generateMonthEndTasks(GenerateMonthEndTasksRequest request) {
        MonthEndTaskGenerationResult result = generateMonthEndTasksUseCase.generate(
                transportHelper.parseMonth(request.getMonth())
        );
        return Response.ok(monthEndRestMapper.toResponse(result)).build();
    }
}
