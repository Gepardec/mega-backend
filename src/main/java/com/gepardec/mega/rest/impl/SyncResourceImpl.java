package com.gepardec.mega.rest.impl;

import com.gepardec.mega.rest.api.SyncResource;
import com.gepardec.mega.service.api.ProjectSyncService;
import com.gepardec.mega.service.api.SyncService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.time.LocalDate;

public class SyncResourceImpl implements SyncResource {

    @Inject
    ProjectSyncService projectSyncService;

    @Inject
    SyncService syncService;

    @Override
    public Response syncProjects(LocalDate date) {
        if(date == null){
            return Response.ok(projectSyncService.generateProjects()).build();
        }

        return Response.ok(projectSyncService.generateProjects(date)).build();
    }

    @Override
    public Response syncEmployees(LocalDate date) {
        syncService.syncEmployees();
        return Response.ok("ok").build();
    }

    @Override
    public Response syncEnterpriseEntries() {
        return null;
    }

    @Override
    public Response syncStepEntries() {
        return null;
    }
}
