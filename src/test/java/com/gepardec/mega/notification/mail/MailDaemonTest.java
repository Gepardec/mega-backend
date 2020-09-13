package com.gepardec.mega.notification.mail;

import com.gepardec.mega.application.producer.ResourceBundleProducer;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.service.api.employee.EmployeeService;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static com.gepardec.mega.notification.mail.Reminder.OM_RELEASE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@QuarkusTest
public class MailDaemonTest {

    @InjectMock
    private ResourceBundleProducer resourceBundleProducer;

    @Inject
    MailDaemon mailDaemon;

    @Inject
    MockMailbox mailbox;

    @ConfigProperty(name = "mega.mail.reminder.pl")
    String projectLeadersMailAddresses;

    @ConfigProperty(name = "mega.mail.reminder.om")
    String omMailAddresses;

    @ConfigProperty(name = "quarkus.mailer.mock")
    boolean mailMockSetting;

    @Inject
    EmployeeService employeeService;

    @BeforeEach
    void init() {
        assertTrue(mailMockSetting, "This test can only run when mail mocking is true");
        when(resourceBundleProducer.getResourceBundle()).thenReturn(ResourceBundle.getBundle("messages"));
        mailbox.clear();
    }


    @Test
    void getNameByMail_usualInput_shouldReturnFirstName() {
        assertEquals("John", MailDaemon.getNameByMail("john.doe@gmail.com"));
    }

    @Test
    void getNameByMail_EmptyInput_shouldReturnFirstName() {
        assertEquals("", MailDaemon.getNameByMail(""));
    }


    @Test
    void sendReminderToOm_mailAddressesAvailable_shouldSendMail() {
        List<String> mailAddresses = List.of(omMailAddresses.split("\\,"));
        mailDaemon.sendReminderToOm(OM_RELEASE);
        assertAll(
                () -> assertEquals(mailAddresses.size(), mailbox.getTotalMessagesSent()),
                () -> mailAddresses.forEach(mailAddress ->
                        assertEquals("UNIT-TEST: Reminder: Freigaben durchführen", mailbox.getMessagesSentTo(mailAddresses.get(0)).get(0)
                                .getSubject()))
        );
    }

    @Test
    void sendReminderToPl_mailAddressesAvailable_shouldSendMail() {
        List<String> mailAddresses = List.of(projectLeadersMailAddresses.split("\\,"));
        mailDaemon.sendReminderToPl();
        assertAll(
                () -> assertEquals(mailAddresses.size(), mailbox.getTotalMessagesSent()),
                () -> mailAddresses.forEach(mailAddress ->
                        assertEquals("UNIT-TEST: Reminder: Projekte kontrollieren und abrechnen", mailbox.getMessagesSentTo(mailAddress).get(0)
                                .getSubject()))
        );
    }

    @Disabled("till userNotifaction is enabled")
    @Test
    void sendReminderToUser() {
        List<String> addresses = employeeService.getAllActiveEmployees().stream()
                .map(Employee::email)
                .collect(Collectors.toList());

        mailDaemon.sendReminderToUser();
        assertAll(
                () -> assertEquals(addresses.size(), mailbox.getTotalMessagesSent()),
                () -> addresses.forEach(address ->
                        assertEquals("UNIT-TEST: Friendly Reminder: Buchungen kontrollieren", mailbox.getMessagesSentTo(address).get(0).getSubject()))
        );
    }
}
