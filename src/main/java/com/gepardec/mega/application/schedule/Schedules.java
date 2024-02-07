package com.gepardec.mega.application.schedule;

import com.gepardec.mega.notification.mail.ReminderEmailSender;
import com.gepardec.mega.notification.mail.receiver.MailReceiver;
import com.gepardec.mega.rest.api.SyncResource;
import com.gepardec.mega.service.api.EnterpriseSyncService;
import com.gepardec.mega.service.api.PrematureEmployeeCheckSyncService;
import com.gepardec.mega.service.api.ProjectSyncService;
import com.gepardec.mega.service.api.StepEntrySyncService;
import com.gepardec.mega.service.api.SyncService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.concurrent.TimeUnit;

import static com.gepardec.mega.domain.utils.DateUtils.getFirstDayOfCurrentMonth;

/**
 * @author Thomas Herzog <herzog.thomas81@gmail.com>
 * @since 10/3/2020
 */
@Dependent
public class Schedules {

    // Documentation: https://docs.oracle.com/cd/E12058_01/doc/doc.1014/e12030/cron_expressions.htm

    @Inject
    SyncService syncService;

    @Inject
    StepEntrySyncService stepEntrySyncService;


    @Inject
    ProjectSyncService projectSyncService;

    @Inject
    EnterpriseSyncService enterpriseSyncService;

    @Inject
    PrematureEmployeeCheckSyncService prematureEmployeeCheckSyncService;

    @Inject
    ReminderEmailSender reminderEmailSender;

    @Inject
    MailReceiver mailReceiver;

    @Scheduled(identity = "Sync ZEP-Employees with Users in the database every 30 minutes",
            every = "PT30M",
            delay = 15, delayUnit = TimeUnit.SECONDS)
        // We need to wait for liquibase to finish, but is executed in parallel
    void syncEmployees() {
        syncService.syncEmployees();
    }

    @Scheduled(identity = "Generate step entries on the last day of a month at 00:00",
            cron = "0 0 0 L * ? *")
    void generateStepEntries() {
        stepEntrySyncService.generateStepEntries(getFirstDayOfCurrentMonth());
    }

    @Scheduled(identity = "Update project entries every 30 minutes",
            every = "PT30M",
            delay = 30, delayUnit = TimeUnit.SECONDS)
    void generateProjects() {
        projectSyncService.generateProjects(getFirstDayOfCurrentMonth());
    }

    @Scheduled(identity = "Generate enterprise entries on the last day of a month at 00:00",
            cron = "0 0 0 L * ? *")
    void generateEnterpriseEntries() {
        enterpriseSyncService.generateEnterpriseEntries(getFirstDayOfCurrentMonth());
    }

    @Scheduled(identity = "Send E-Mail reminder to Users",
            cron = "0 0 7 ? * MON-FRI")
    void sendReminder() {
        reminderEmailSender.sendReminder();
    }

    @Scheduled(
            identity = "Receive E-Mails sent to employees from ZEP",
            cron = "${mega.mail.receiver.cron-expr}"
    )
    void receiveMails() {
        mailReceiver.retrieveZepEmailsFromInbox();
    }

    @Scheduled(identity = "Take existing PrematureEmployeeChecks and update StepEntries accordingly on the last day of a month at 06:00",
            cron = "0 0 6 L * ? *")
    void syncPrematureEmployeeChecksWithStepEntries() {
        prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(getFirstDayOfCurrentMonth());
    }

    @Scheduled(identity = "Set state of step id 1 to DONE for any employee who was absent the whole month and had no time bookings - on the first day of a month at 07:00",
               cron = "0 0 7 1 * ? *")
    void setStateToDoneForEmployeesWhoWhereAbsentWholeMonth(){
        syncService.syncUpdateEmployeesWithoutTimeBookingsAndAbsentWholeMonth();
    }
}
