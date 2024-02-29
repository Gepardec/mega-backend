package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.configuration.GoogleCloudConfig;
import com.gepardec.mega.rest.api.WatchInboxResource;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.WatchResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RequestScoped
@RolesAllowed("mega-cron:mail")
public class WatchInboxResourceImpl implements WatchInboxResource {

    @Inject
    GoogleCredentials googleCredentials;

    @Inject
    GoogleCloudConfig googleCloudConfig;

    @Inject
    Logger logger;

    @Override
    public WatchResponse watchInbox() {
        try {
            // Build Gmail service
            Gmail gmailService =
                    initializeGmailBuilder()
                            .setApplicationName(googleCloudConfig.getApplicationName())
                            .build();

            // Create Watch Request
            WatchRequest watchRequest = new WatchRequest();
            watchRequest.setLabelFilterBehavior("INCLUDE");
            watchRequest.setLabelIds(googleCloudConfig.getLabelFilter());
            watchRequest.setTopicName(googleCloudConfig.getTopicName());

            // Execute Watch Request
            var res = gmailService.users().watch(googleCloudConfig.getWorkspaceUser(), watchRequest).execute();
            logger.info("Watch Response: {}", res);

            return res;
        } catch (IOException | GeneralSecurityException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    @Override
    public Void stopInbox() {
        try {
            // Build Gmail service
            Gmail gmailService =
                    initializeGmailBuilder()
                            .setApplicationName(googleCloudConfig.getApplicationName())
                            .build();

            // Execute Stop Watch Request
            var res = gmailService.users().stop(googleCloudConfig.getWorkspaceUser()).execute();
            logger.info("Stop Response: {}", res);

            return res;
        } catch (IOException | GeneralSecurityException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    private Gmail.Builder initializeGmailBuilder() throws GeneralSecurityException, IOException {
        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(googleCredentials)
        );
    }
}
