package com.gepardec.mega.hexagon.application.schedule;

import com.gepardec.mega.hexagon.project.application.ReconcileLeadsService;
import com.gepardec.mega.hexagon.project.application.SyncProjectsService;
import com.gepardec.mega.hexagon.project.domain.port.inbound.ProjectSyncResult;
import com.gepardec.mega.hexagon.project.domain.port.inbound.ReconcileLeadsResult;
import com.gepardec.mega.hexagon.project.domain.port.inbound.ReconcileLeadsUseCase;
import com.gepardec.mega.hexagon.project.domain.port.inbound.SyncProjectsUseCase;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.project.domain.port.outbound.UserLookupPort;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ZepProjectPort;
import com.gepardec.mega.hexagon.user.application.SyncUsersService;
import com.gepardec.mega.hexagon.user.application.UserSyncConfig;
import com.gepardec.mega.hexagon.user.domain.port.inbound.SyncUsersUseCase;
import com.gepardec.mega.hexagon.user.domain.port.inbound.UserSyncResult;
import com.gepardec.mega.hexagon.user.domain.port.outbound.PersonioEmployeePort;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.user.domain.port.outbound.ZepEmployeePort;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SyncScheduler {

    private final SyncUsersUseCase syncUsersUseCase;
    private final SyncProjectsUseCase syncProjectsUseCase;
    private final ReconcileLeadsUseCase reconcileLeadsUseCase;

    @Inject
    public SyncScheduler(
            ZepEmployeePort zepEmployeePort,
            PersonioEmployeePort personioEmployeePort,
            UserRepository userRepository,
            ZepProjectPort zepProjectPort,
            ProjectRepository projectRepository,
            UserLookupPort userLookupPort,
            @ConfigProperty(name = "mega.mail.reminder.om") List<String> officeManagementUsernames
    ) {
        UserSyncConfig config = new UserSyncConfig(officeManagementUsernames);
        this.syncUsersUseCase = new SyncUsersService(zepEmployeePort, personioEmployeePort, userRepository, config);
        this.syncProjectsUseCase = new SyncProjectsService(zepProjectPort, projectRepository);
        this.reconcileLeadsUseCase = new ReconcileLeadsService(zepProjectPort, projectRepository, userLookupPort, userRepository);
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
        Log.infof("[SyncScheduler] user-sync: added=%d updated=%d disabled=%d (duration=%dms)",
                userResult.added(), userResult.updated(), userResult.disabled(), userMs);

        Instant t1 = Instant.now();
        ProjectSyncResult projectResult = syncProjectsUseCase.sync();
        long projectMs = Duration.between(t1, Instant.now()).toMillis();
        Log.infof("[SyncScheduler] project-sync: created=%d updated=%d (duration=%dms)",
                projectResult.created(), projectResult.updated(), projectMs);

        Instant t2 = Instant.now();
        ReconcileLeadsResult reconcileResult = reconcileLeadsUseCase.reconcile();
        long reconcileMs = Duration.between(t2, Instant.now()).toMillis();
        Log.infof("[SyncScheduler] reconcile-leads: resolved=%d skipped=%d rolesAdded=%d rolesRevoked=%d (duration=%dms)",
                reconcileResult.resolved(), reconcileResult.skipped(),
                reconcileResult.rolesAdded(), reconcileResult.rolesRevoked(), reconcileMs);

        Log.infof("[SyncScheduler] sync cycle complete: total duration=%dms",
                Duration.between(cycleStart, Instant.now()).toMillis());
    }
}
