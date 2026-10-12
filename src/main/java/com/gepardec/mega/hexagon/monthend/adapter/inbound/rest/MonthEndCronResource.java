package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.CronApi;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GenerateMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import io.quarkus.oidc.Tenant;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

/**
 * Machine-to-machine month-end endpoints, secured by the {@code mega-cron}
 * client-credentials scheme. Kept apart from {@link MonthEndResource} so the
 * contract can carry a dedicated {@code Cron} tag: consumers filter on that tag
 * to keep these operations out of generated browser clients.
 */
@RequestScoped
@Tenant("mega-cron")
@RolesAllowed("mega-cron:sync")
public class MonthEndCronResource implements CronApi {

    private final GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase;
    private final MonthEndRestTransportHelper transportHelper;
    private final MonthEndRestMapper monthEndRestMapper;

    @Inject
    public MonthEndCronResource(
            GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase,
            MonthEndRestTransportHelper transportHelper,
            MonthEndRestMapper monthEndRestMapper
    ) {
        this.generateMonthEndTasksUseCase = generateMonthEndTasksUseCase;
        this.transportHelper = transportHelper;
        this.monthEndRestMapper = monthEndRestMapper;
    }

    @Override
    public Response generateMonthEndTasks(String month) {
        MonthEndTaskGenerationResult result = generateMonthEndTasksUseCase.generate(
                transportHelper.parseMonth(month)
        );

        return Response.ok(monthEndRestMapper.toDto(result)).build();
    }
}
