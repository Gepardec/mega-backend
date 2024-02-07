package com.gepardec.mega.rest.impl;

import com.gepardec.mega.rest.api.SyncResource;
import com.gepardec.mega.rest.model.EmployeeDto;
import com.gepardec.mega.service.api.EnterpriseSyncService;
import com.gepardec.mega.service.api.PrematureEmployeeCheckSyncService;
import com.gepardec.mega.service.api.ProjectSyncService;
import com.gepardec.mega.service.api.StepEntrySyncService;
import com.gepardec.mega.service.api.SyncService;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Function;

import static com.gepardec.mega.domain.utils.DateUtils.getFirstDayOfCurrentMonth;
import static com.gepardec.mega.domain.utils.DateUtils.getFirstOfYearMonth;

@RequestScoped
@IfBuildProperty(name = "mega.endpoint.test.enable", stringValue = "true", enableIfMissing = true)
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
    public Response syncProjects(YearMonth from, YearMonth to) {
        if (from == null) {
            return Response.ok(projectSyncService.generateProjects()).build();
        }

        if (to == null) {
            return Response.ok(projectSyncService.generateProjects(from.atDay(1))).build();
        }

        while (from.isBefore(to)) {
            projectSyncService.generateProjects(from.atDay(1));
            from = from.plusMonths(1);
        }

        return Response.ok(projectSyncService.generateProjects(from.atDay(1))).build();
    }

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

    private Response syncFromTo(Function<LocalDate, Boolean> syncFunction, YearMonth from, YearMonth to) {
        if (from == null) {
            return Response.ok(syncFunction.apply(getFirstDayOfCurrentMonth())).build();
        }

        if (to == null) {
            return Response.ok(syncFunction.apply(getFirstOfYearMonth(from))).build();
        }

        while (from.isBefore(to)) {
            syncFunction.apply(getFirstOfYearMonth(from));
            from = from.plusMonths(1);
        }

        return Response.ok(syncFunction.apply(getFirstOfYearMonth(from))).build();
    }
}
