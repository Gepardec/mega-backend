package com.gepardec.mega.application.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class OAuthConfig {

    @Inject
    @ConfigProperty(name = "mega.oauth.client-id")
    String clientId;

    @Inject
    @ConfigProperty(name = "mega.oauth.issuer")
    String issuer;

    @Inject
    @ConfigProperty(name = "mega.oauth.scope")
    String scope;

    public String getClientId() {
        return clientId;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getScope() {
        return scope;
    }
}
