package com.gepardec.mega.hexagon.user.adapter.inbound;

import com.gepardec.mega.hexagon.user.application.SyncUsersService;
import com.gepardec.mega.hexagon.user.application.UserSyncConfig;
import com.gepardec.mega.hexagon.user.domain.port.inbound.SyncUsersUseCase;
import com.gepardec.mega.hexagon.user.domain.port.outbound.PersonioEmployeePort;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.user.domain.port.outbound.ZepEmployeePort;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class UserSyncScheduler {

    private final SyncUsersUseCase syncUsersUseCase;

    @Inject
    public UserSyncScheduler(
            ZepEmployeePort zepEmployeePort,
            PersonioEmployeePort personioEmployeePort,
            UserRepository userRepository,
            @ConfigProperty(name = "mega.mail.reminder.om")
            List<String> officeManagementUsernames
    ) {
        UserSyncConfig config = new UserSyncConfig(officeManagementUsernames);
        this.syncUsersUseCase = new SyncUsersService(zepEmployeePort, personioEmployeePort, userRepository, config);
    }

    @Scheduled(
            identity = "Sync Users to the database every 30 minutes",
            every = "PT30M",
            delay = 15, delayUnit = TimeUnit.SECONDS    // We need to wait for liquibase to finish, but is executed in parallel
    )
    public void syncUsers() {
        syncUsersUseCase.sync();
    }
}
