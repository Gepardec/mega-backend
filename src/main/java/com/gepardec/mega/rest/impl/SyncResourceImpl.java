package com.gepardec.mega.rest.impl;

import com.gepardec.mega.domain.utils.DateUtils;
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
    public Response generateEnterpriseEntries(YearMonth from, YearMonth to) {
        if (from == null) {
            return Response.ok(enterpriseSyncService.generateEnterpriseEntries(LocalDate.now().withDayOfMonth(1))).build();
        }
        if (to == null) {
            return Response.ok(enterpriseSyncService.generateEnterpriseEntries(from.atDay(1))).build();
        }

        while (from.isBefore(to)) {
            enterpriseSyncService.generateEnterpriseEntries(from.atDay(1));
            from = from.plusMonths(1);
        }

        return Response.ok(enterpriseSyncService.generateEnterpriseEntries(from.atDay(1))).build();
    }

    @Override
    public Response generateStepEntries(YearMonth from, YearMonth to) {
        if (from == null) {
            return Response.ok(stepEntrySyncService.generateStepEntriesFromEndpoint()).build();
        }
        if (to == null) {
            return Response.ok(stepEntrySyncService.generateStepEntriesFromEndpoint(from)).build();
        }

        while (from.isBefore(to)) {
            stepEntrySyncService.generateStepEntriesFromEndpoint(from);
            from = from.plusMonths(1);
        }

        return Response.ok(stepEntrySyncService.generateStepEntriesFromEndpoint(from)).build();
    }

    @Override
    public Response syncPrematureEmployeeChecks(YearMonth from, YearMonth to) {
        if (from == null) {
            prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(DateUtils.getCurrentYearMonth());
            return Response.ok().build();
        }

        if (to == null) {
            prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(from);
            return Response.ok().build();
        }

        do {
            prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(from);
            from = from.plusMonths(1);
        } while (from.compareTo(to) <= 0);

        return Response.ok().build();
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
}
