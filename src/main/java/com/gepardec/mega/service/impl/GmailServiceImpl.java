package com.gepardec.mega.service.impl;

import com.gepardec.mega.application.configuration.GoogleCloudConfig;
import com.gepardec.mega.service.api.GmailService;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.WatchResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.slf4j.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Dependent
public class GmailServiceImpl implements GmailService {

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

        return new WatchResponse();
    }

    @Override
    public Void stopWatchingInbox() {
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
