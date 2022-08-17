package com.gepardec.mega.rest.impl;

import com.gepardec.mega.rest.api.SyncResource;
import com.gepardec.mega.service.api.EnterpriseSyncService;
import com.gepardec.mega.service.api.ProjectSyncService;
import com.gepardec.mega.service.api.StepEntrySyncService;
import com.gepardec.mega.service.api.SyncService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.YearMonth;

public class SyncResourceImpl implements SyncResource {

    @Inject
    ProjectSyncService projectSyncService;

    @Inject
    SyncService syncService;

    @Inject
    StepEntrySyncService stepEntrySyncService;

    @Inject
    EnterpriseSyncService enterpriseSyncService;

    @Override
    public Response syncProjects(YearMonth from, YearMonth to) {
        if (from == null) return Response.ok(projectSyncService.generateProjects()).build();

        if(to == null) return Response.ok(projectSyncService.generateProjects(from.atDay(1))).build();

        while(from.isBefore(to)){
            projectSyncService.generateProjects(from.atDay(1));
            from = from.plusMonths(1);
        }

        return Response.ok("ok").build();
    }

    @Override
    public Response syncEmployees() {

        syncService.syncEmployees();
        return Response.ok("ok").build();
    }


    @Override
    public Response generateEnterpriseEntries() {
        enterpriseSyncService.generateEnterpriseEntries();
        return Response.ok("ok").build();
    }

    @Override
    public Response generateSyncEntries() {
        stepEntrySyncService.generateStepEntriesFromEndpoint();
        return Response.ok("ok").build();
    }


}
