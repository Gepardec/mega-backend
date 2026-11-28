package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.CronApi;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GenerateMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import io.quarkus.oidc.Tenant;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.YearMonth;

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
    private final MonthEndRestMapper monthEndRestMapper;

    @Inject
    public MonthEndCronResource(
            GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase,
            MonthEndRestMapper monthEndRestMapper
    ) {
        this.generateMonthEndTasksUseCase = generateMonthEndTasksUseCase;
        this.monthEndRestMapper = monthEndRestMapper;
    }

    @Override
    public Response generateMonthEndTasks(YearMonth month) {
        MonthEndTaskGenerationResult result = generateMonthEndTasksUseCase.generate(month);

        return Response.ok(monthEndRestMapper.toDto(result)).build();
    }
}
