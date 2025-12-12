package com.gepardec.mega.application.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URL;

@ApplicationScoped
public class ZepConfig {

    @Inject
    @ConfigProperty(name = "mega.zep.admin-token")
    String authorizationToken;

    @Inject
    @ConfigProperty(name = "mega.zep.origin")
    URL origin;

    @Inject
    @ConfigProperty(name = "mega.zep.soap-path")
    String soapPath;

    @Inject
    @ConfigProperty(name = "mega.zep.rest-token")
    String restToken;

    String abc;


    public String getRestBearerToken() {
        return restToken;
    }

    public String getUrlAsString() {
        return "%s%s".formatted(origin.toString(), soapPath);
    }

    public String getUrlForFrontend() {
        return origin.toString();
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

}
