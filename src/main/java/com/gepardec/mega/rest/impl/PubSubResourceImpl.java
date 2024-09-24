package com.gepardec.mega.rest.impl;

import com.gepardec.mega.notification.mail.receiver.MailReceiver;
import com.gepardec.mega.rest.api.PubSubResource;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;

import java.time.LocalDateTime;

@Authenticated
public class PubSubResourceImpl implements PubSubResource {

    @Inject
    Logger logger;

    @Inject
    MailReceiver mailReceiver;

    @Override
    public Response gmailMessageReceivedWebhook(String payload) {
        try {
            logNotificationReceived(payload);
            mailReceiver.retrieveZepEmailsFromInbox();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response.serverError().entity(e.getMessage()).build();
        }

        return Response.ok().build();
    }

    @Override
    public LocalDateTime ping(String payload) {
        logNotificationReceived(payload);
        return LocalDateTime.now();
    }

    private void logNotificationReceived(String payload) {
        logger.info("Received notification from Pub/Sub: {}", payload != null ? payload.strip() : null);
    }
}
