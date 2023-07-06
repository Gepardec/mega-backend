package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.configuration.ApplicationConfig;
import com.gepardec.mega.rest.api.ApplicationInfoResource;
import com.gepardec.mega.rest.model.ApplicationInfoDto;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.Duration;
import java.time.LocalDateTime;

@RequestScoped
@PermitAll
public class ApplicationInfoResourceImpl implements ApplicationInfoResource {

    @Inject
    ApplicationConfig applicationConfig;

    @Override
    public Response get() {
        final Duration upTime = Duration.between(applicationConfig.getStartAt(), LocalDateTime.now());

        final ApplicationInfoDto applicationInfoDto = ApplicationInfoDto.builder()
                .version(applicationConfig.getVersion())
                .buildDate(applicationConfig.getBuildDate())
                .commit(applicationConfig.getCommit())
                .branch(applicationConfig.getBranch())
                .startedAt(applicationConfig.getStartAt())
                .upTime(upTime)
                .build();
        return Response.ok(applicationInfoDto).build();
    }
}
