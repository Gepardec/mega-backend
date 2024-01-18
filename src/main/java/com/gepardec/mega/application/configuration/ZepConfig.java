package com.gepardec.mega.application.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.ConfigProvider;
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

    public String getUrlAsString() {
        return String.format("%s%s", origin.toString(), soapPath);
    }

    public String getUrlForFrontend() {
        return origin.toString();
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

}
