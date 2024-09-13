package com.gepardec.mega.application.schedule;

import com.gepardec.mega.notification.mail.ReminderEmailSender;
import com.gepardec.mega.service.api.GmailService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
public class MailSchedules {

    @Inject
    ReminderEmailSender reminderEmailSender;

    @Inject
    GmailService gmailService;

    @Scheduled(
            identity = "Send E-Mail reminder to users",
            cron = "0 0 7 ? * MON-FRI"
    )
    void sendReminder() {
        reminderEmailSender.sendReminder();
    }

    @Scheduled(
            identity = "Renew the mailbox watch for the Gmail API every day at 06:00",
            cron = "0 0 6 * * ? *"
    )
    void renewMailboxWatch() {
        gmailService.watchInbox();
    }
}
