package com.gepardec.mega.rest.impl;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateClarificationFromZepMailUseCase;
import com.gepardec.mega.hexagon.notification.application.port.inbound.SendScheduledRemindersUseCase;
import com.gepardec.mega.rest.api.MailResource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequestScoped
@RolesAllowed("mega-cron:mail")
public class MailResourceImpl implements MailResource {

    @Inject
    SendScheduledRemindersUseCase sendScheduledRemindersUseCase;

    @Inject
    CreateClarificationFromZepMailUseCase createClarificationFromZepMailUseCase;

    @Override
    public Response sendReminder() {
        try {
            sendScheduledRemindersUseCase.send(LocalDate.now());
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }

        return Response.ok().build();
    }

    @Override
    public Response retrieveZepEmailsFromInbox() {
        try {
            createClarificationFromZepMailUseCase.create();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }

        return Response.ok().build();
    }

    @Override
    public LocalDateTime ping() {
        return LocalDateTime.now();
    }
}
