package com.gepardec.mega.rest.impl;

import com.gepardec.mega.rest.api.WatchInboxResource;
import com.gepardec.mega.service.api.GmailService;
import com.google.api.services.gmail.model.WatchResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

@RequestScoped
@RolesAllowed("mega-cron:mail")
public class WatchInboxResourceImpl implements WatchInboxResource {

    @Inject
    GmailService gmailService;

    @Override
    public WatchResponse watchInbox() {
        return gmailService.watchInbox();
    }

    @Override
    public Void stopInbox() {
        return gmailService.stopWatchingInbox();
    }
}
