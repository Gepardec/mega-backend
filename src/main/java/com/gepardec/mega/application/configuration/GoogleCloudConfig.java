package com.gepardec.mega.application.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@ApplicationScoped
public class GoogleCloudConfig {

    @Inject
    @ConfigProperty(name = "mega.google.service-account-key")
    String serviceAccountKey;

    @Inject
    @ConfigProperty(name = "mega.google.gmail.api.application-name")
    String applicationName;

    @Inject
    @ConfigProperty(name = "mega.google.gmail.api.workspace-user")
    String workspaceUser;

    @Inject
    @ConfigProperty(name = "mega.google.gmail.api.label-filter")
    List<String> labelFilter;

    @Inject
    @ConfigProperty(name = "mega.google.pubsub.topic")
    String topicName;

    public String getServiceAccountKey() {
        return serviceAccountKey;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getWorkspaceUser() {
        return workspaceUser;
    }

    public List<String> getLabelFilter() {
        return labelFilter;
    }

    public String getTopicName() {
        return topicName;
    }
}
