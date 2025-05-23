package com.gepardec.mega.rest.impl;

import com.gepardec.mega.rest.api.SyncResource;
import com.gepardec.mega.rest.model.EmployeeDto;
import com.gepardec.mega.service.api.EnterpriseSyncService;
import com.gepardec.mega.service.api.PrematureEmployeeCheckSyncService;
import com.gepardec.mega.service.api.ProjectSyncService;
import com.gepardec.mega.service.api.StepEntrySyncService;
import com.gepardec.mega.service.api.SyncService;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Function;

@RequestScoped
@IfBuildProperty(name = "mega.endpoint.test.enable", stringValue = "true", enableIfMissing = true)
@RolesAllowed("mega-cron:sync")
public class SyncResourceImpl implements SyncResource {

    @Inject
    ProjectSyncService projectSyncService;

    @Inject
    SyncService syncService;

    @Inject
    StepEntrySyncService stepEntrySyncService;

    @Inject
    EnterpriseSyncService enterpriseSyncService;

    @Inject
    PrematureEmployeeCheckSyncService prematureEmployeeCheckSyncService;

    @Override
    public Response syncEmployees() {
        syncService.syncEmployees();
        return Response.ok("ok").build();
    }

    @Override
    public Response syncProjects(YearMonth from, YearMonth to) {
        return syncFromTo(projectSyncService::generateProjects, from, to);
    }

    @Override
    public Response generateEnterpriseEntries(YearMonth from, YearMonth to) {
        return syncFromTo(enterpriseSyncService::generateEnterpriseEntries, from, to);
    }

    @Override
    public Response generateStepEntries(YearMonth from, YearMonth to) {
        return syncFromTo(stepEntrySyncService::generateStepEntries, from, to);
    }

    @Override
    public Response syncPrematureEmployeeChecks(YearMonth from, YearMonth to) {
        return syncFromTo(prematureEmployeeCheckSyncService::syncPrematureEmployeeChecksWithStepEntries, from, to);
    }

    @Override
    public Response syncAll(YearMonth from, YearMonth to) {
        syncEmployees();
        syncProjects(from, to);
        generateEnterpriseEntries(from, to);
        generateStepEntries(from, to);

        return Response.ok("ok").build();
    }

    @Override
    public List<EmployeeDto> updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth() {
        return syncService.syncUpdateEmployeesWithoutTimeBookingsAndAbsentWholeMonth();
    }

    @Override
    public LocalDateTime ping() {
        return LocalDateTime.now();
    }

    private Response syncFromTo(Function<YearMonth, Boolean> syncFunction, YearMonth from, YearMonth to) {
        if (from == null) {
            return Response.ok(syncFunction.apply(YearMonth.now())).build();
        }

        if (to == null) {
            return Response.ok(syncFunction.apply(from)).build();
        }

        while (from.isBefore(to)) {
            syncFunction.apply(from);
            from = from.plusMonths(1);
        }

        return Response.ok(syncFunction.apply(from)).build();
    }
}
