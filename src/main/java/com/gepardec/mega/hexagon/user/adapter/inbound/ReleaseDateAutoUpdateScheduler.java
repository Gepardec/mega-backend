package com.gepardec.mega.hexagon.user.adapter.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.application.port.inbound.AutoUpdateReleaseDatesResult;
import com.gepardec.mega.hexagon.user.application.port.inbound.AutoUpdateReleaseDatesUseCase;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ReleaseDateAutoUpdateScheduler {

    private final AutoUpdateReleaseDatesUseCase autoUpdateReleaseDatesUseCase;

    @Inject
    public ReleaseDateAutoUpdateScheduler(AutoUpdateReleaseDatesUseCase autoUpdateReleaseDatesUseCase) {
        this.autoUpdateReleaseDatesUseCase = autoUpdateReleaseDatesUseCase;
    }

    @Scheduled(cron = "0 0 6 15-31 * ?")
    void autoUpdateReleaseDates() {
        Log.info("Starting scheduled release-date auto-update run");

        AutoUpdateReleaseDatesResult result = autoUpdateReleaseDatesUseCase.autoUpdate();

        for (UserId updatedUserId : result.updatedUserIds()) {
            Log.infof(
                    "Scheduled release-date auto update succeeded for employee=%s releaseDate=%s",
                    updatedUserId.value(),
                    result.releaseDate()
            );
        }

        for (UserId failedUserId : result.failedUserIds()) {
            Log.errorf(
                    "Scheduled release-date auto update failed for employee=%s releaseDate=%s",
                    failedUserId.value(),
                    result.releaseDate()
            );
        }

        Log.infof(
                "Finished scheduled release-date auto-update run for payrollMonth=%s updated=%d failed=%d",
                result.payrollMonth(),
                result.updatedUserIds().size(),
                result.failedUserIds().size()
        );
    }
}
