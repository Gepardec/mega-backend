package com.gepardec.mega.hexagon.notification.adapter.inbound;

import com.gepardec.mega.hexagon.notification.application.port.inbound.SendScheduledRemindersUseCase;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;

@ApplicationScoped
public class ReminderEmailSchedulerAdapter {

    private final SendScheduledRemindersUseCase sendScheduledRemindersUseCase;

    @Inject
    public ReminderEmailSchedulerAdapter(SendScheduledRemindersUseCase sendScheduledRemindersUseCase) {
        this.sendScheduledRemindersUseCase = sendScheduledRemindersUseCase;
    }

    @Scheduled(
            identity = "Send E-Mail reminder to users",
            cron = "0 0 7 ? * MON-FRI"
    )
    void sendReminder() {
        LocalDate today = LocalDate.now();
        Log.infof("Starting scheduled reminder dispatch for %s", today);
        sendScheduledRemindersUseCase.send(today);
        Log.infof("Finished scheduled reminder dispatch for %s", today);
    }
}
