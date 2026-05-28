package com.gepardec.mega.application.schedule;

import com.gepardec.mega.service.api.GmailService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
public class MailSchedules {

    @Inject
    GmailService gmailService;

    @Scheduled(
            identity = "Renew the mailbox watch for the Gmail API every day at 06:00",
            cron = "0 0 6 * * ? *"
    )
    void renewMailboxWatch() {
        gmailService.watchInbox();
    }
}
