package com.gepardec.mega;

import com.gepardec.mega.communication.MailSender;
import com.gepardec.mega.communication.Reminder;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
//FIXME set system property surefire
class MailSenderTest {

    private static final String TO = "werner.bruckmueller@gepardec.com";

    @ConfigProperty(name = "quarkus.mailer.mock")
    boolean mailMockSetting;

    @Inject
    MailSender mailSender;

    @Inject
    MockMailbox mailbox;

    @BeforeEach
    void init() {
        mailbox.clear();
    }

    @Test
    void sendMail_commonCase_shouldContainExpectedData() {
        assertTrue(mailMockSetting);
        mailSender.sendReminder(TO, "Jamal", Reminder.EMPLOYEE_CHECK_PROJECTTIME);
        List<Mail> sent = mailbox.getMessagesSentTo(TO);
        assertAll(
                () -> assertEquals(1, sent.size()),
                () -> assertTrue(sent.get(0).getHtml().startsWith("<p>Hallo Jamal")),
                () -> assertTrue(sent.get(0).getSubject().contains("Friendly Reminder"))
        );
    }

    @Test
    void sendMail_toPL_shouldCountainPlData() {
        assertTrue(mailMockSetting);
        mailSender.sendReminder(TO, "Simba", Reminder.PL_PROJECT_CONTROLLING);
    }

    @Test
    void sendMail_toOmControlContent() {
        assertTrue(mailMockSetting);
        mailSender.sendReminder(TO, "Garfield", Reminder.OM_CONTROL_EMPLOYEES_CONTENT);
    }

    @Test
    void sendMail_toOmRelease() {
        assertTrue(mailMockSetting);
        mailSender.sendReminder(TO, "Pacman", Reminder.OM_RELEASE);
    }

    @Test
    void sendMail_toOmAdministrative() {
        assertTrue(mailMockSetting);
        mailSender.sendReminder(TO, "Mrs. Piggy", Reminder.OM_ADMINISTRATIVE);
    }

    @Test
    void sendMail_toOmSalary() {
        assertTrue(mailMockSetting);
        mailSender.sendReminder(TO, "Mrs. Piggy", Reminder.OM_SALARY);
    }


    @Test
    void sendMail_send100Mails_allMailsShouldBeInMailBox() {
        assertTrue(mailMockSetting);
        for (int i = 0; i < 100; i++) {
            mailSender.sendReminder(TO, "Jamal", Reminder.EMPLOYEE_CHECK_PROJECTTIME);
        }
        List<Mail> sent = mailbox.getMessagesSentTo(TO);
        assertEquals(100, sent.size());
    }
}