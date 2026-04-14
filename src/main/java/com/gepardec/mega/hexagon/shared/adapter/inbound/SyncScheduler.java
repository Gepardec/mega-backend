package com.gepardec.mega.hexagon.shared.adapter.inbound;

import com.gepardec.mega.hexagon.project.application.port.inbound.ProjectLeadSyncResult;
import com.gepardec.mega.hexagon.project.application.port.inbound.ProjectSyncResult;
import com.gepardec.mega.hexagon.project.application.port.inbound.SyncProjectLeadsUseCase;
import com.gepardec.mega.hexagon.project.application.port.inbound.SyncProjectsUseCase;
import com.gepardec.mega.hexagon.user.application.port.inbound.SyncUsersUseCase;
import com.gepardec.mega.hexagon.user.application.port.inbound.UserSyncResult;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SyncScheduler {

    private final SyncUsersUseCase syncUsersUseCase;
    private final SyncProjectsUseCase syncProjectsUseCase;
    private final SyncProjectLeadsUseCase syncProjectLeadsUseCase;

    @Inject
    public SyncScheduler(
            SyncUsersUseCase syncUsersUseCase,
            SyncProjectsUseCase syncProjectsUseCase,
            SyncProjectLeadsUseCase syncProjectLeadsUseCase
    ) {
        this.syncUsersUseCase = syncUsersUseCase;
        this.syncProjectsUseCase = syncProjectsUseCase;
        this.syncProjectLeadsUseCase = syncProjectLeadsUseCase;
    }

    @Scheduled(
            identity = "Sync users, projects, and leads every 30 minutes",
            every = "PT30M",
            delay = 15, delayUnit = TimeUnit.SECONDS
    )
    public void sync() {
        Instant cycleStart = Instant.now();

        Instant t0 = Instant.now();
        UserSyncResult userResult = syncUsersUseCase.sync();
        long userMs = Duration.between(t0, Instant.now()).toMillis();
        Log.infof("user-sync: added=%d updated=%d unchanged=%d skippedNoEmail=%d personioLinked=%d (duration=%dms)",
                userResult.added(), userResult.updated(), userResult.unchanged(),
                userResult.skippedNoEmail(), userResult.personioLinked(), userMs);

        Instant t1 = Instant.now();
        ProjectSyncResult projectResult = syncProjectsUseCase.sync();
        long projectMs = Duration.between(t1, Instant.now()).toMillis();
        Log.infof("project-sync: created=%d updated=%d unchanged=%d (duration=%dms)",
                projectResult.created(), projectResult.updated(), projectResult.unchanged(), projectMs);

        Instant t2 = Instant.now();
        ProjectLeadSyncResult projectLeadSyncResult = syncProjectLeadsUseCase.sync();
        long projectLeadSyncMs = Duration.between(t2, Instant.now()).toMillis();
        Log.infof("project-lead-sync: resolved=%d skipped=%d rolesAdded=%d rolesRevoked=%d (duration=%dms)",
                projectLeadSyncResult.resolved(), projectLeadSyncResult.skipped(),
                projectLeadSyncResult.rolesAdded(), projectLeadSyncResult.rolesRevoked(), projectLeadSyncMs);

        Log.infof("sync cycle complete: total duration=%dms",
                Duration.between(cycleStart, Instant.now()).toMillis());
    }
}
