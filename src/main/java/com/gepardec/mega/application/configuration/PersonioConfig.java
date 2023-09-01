package com.gepardec.mega.application.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class PersonioConfig {

    @Inject
    @ConfigProperty(name = "mega.personio.api.client.secret")
    String clientSecret;

    @Inject
    @ConfigProperty(name = "mega.personio.api.client.id")
    String clientId;

    @Inject
    @ConfigProperty(name = "mega.personio.token.expires-in-minutes")
    long expiresInMinutes;

    public String getClientSecret() {
        return clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public long getExpiresInMinutes() {
        return expiresInMinutes;
    }
}
