package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.configuration.ApplicationConfig;
import com.gepardec.mega.application.configuration.NotificationConfig;
import com.gepardec.mega.application.configuration.OAuthConfig;
import com.gepardec.mega.application.configuration.ZepConfig;
import com.gepardec.mega.rest.api.ConfigResource;
import com.gepardec.mega.rest.model.ConfigDto;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

/**
 * Provides configuration for the frontend.
 */
@RequestScoped
@PermitAll
public class ConfigResourceImpl implements ConfigResource {

    @Inject
    OAuthConfig oauthConfig;

    @Inject
    ApplicationConfig applicationConfig;

    @Inject
    ZepConfig zepConfig;

    @Inject
    NotificationConfig notificationConfig;

    @Override
    public Response get() {
        final ConfigDto configDto = ConfigDto.builder()
                .zepOrigin(zepConfig.getUrlForFrontend())
                .clientId(oauthConfig.getClientId())
                .issuer(oauthConfig.getIssuer())
                .scope(oauthConfig.getScope())
                .version(applicationConfig.getVersion())
                .omMailAddresses(notificationConfig.getOmMailAddresses())
                .subjectPrefix(notificationConfig.getSubjectPrefix())
                .megaDashUrl(notificationConfig.getMegaDashUrl())
                .build();

        return Response.ok(configDto).build();
    }
}
