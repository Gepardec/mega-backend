package com.gepardec.mega.application.producer;

import com.gepardec.mega.application.configuration.GoogleCloudConfig;
import com.google.api.services.gmail.GmailScopes;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

@ApplicationScoped
public class GoogleCredentialsProducer {

    @Inject
    GoogleCloudConfig googleCloudConfig;

    @Produces
    @ApplicationScoped
    public GoogleCredentials produceGoogleCredentials() throws IOException {
        return GoogleCredentials.fromStream(new ByteArrayInputStream(googleCloudConfig.getServiceAccountKey().getBytes()))
                .createScoped(Collections.singleton(GmailScopes.GMAIL_READONLY))
                .createDelegated(googleCloudConfig.getWorkspaceUser());
    }
}
