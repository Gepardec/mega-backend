package com.gepardec.mega.rest.impl;

import com.gepardec.mega.notification.mail.ReminderEmailSender;
import com.gepardec.mega.notification.mail.receiver.MailReceiver;
import com.gepardec.mega.rest.api.MailResource;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;

import java.time.LocalDateTime;

@RequestScoped
@RolesAllowed("mega-cron:mail")
public class MailResourceImpl implements MailResource {

    @Inject
    ReminderEmailSender reminderEmailSender;

    @Inject
    MailReceiver mailReceiver;

    @Inject
    Logger logger;

    @Override
    public Response sendReminder() {
        try {
            reminderEmailSender.sendReminder();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.serverError().entity(e.getMessage()).build();
        }

        return Response.ok().build();
    }

    @Override
    public Response retrieveZepEmailsFromInbox() {
        try {
            mailReceiver.retrieveZepEmailsFromInbox();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.serverError().entity(e.getMessage()).build();
        }

        return Response.ok().build();
    }

    @Override
    public LocalDateTime ping() {
        return LocalDateTime.now();
    }
}
