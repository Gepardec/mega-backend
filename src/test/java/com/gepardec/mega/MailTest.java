package com.gepardec.mega;

import com.gepardec.mega.communication.MailSender;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class MailTest {

    @Inject
    private MailSender mailSender;

    @Test
    void sendMail() {
        mailSender.send();
    }
}