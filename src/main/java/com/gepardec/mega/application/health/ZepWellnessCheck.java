package com.gepardec.mega.application.health;

import com.gepardec.mega.zep.rest.client.ZepLivenessRestClient;
import io.smallrye.health.api.Wellness;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Wellness
@ApplicationScoped
public class ZepWellnessCheck implements HealthCheck {

    @Inject
    @RestClient
    ZepLivenessRestClient zepLivenessRestClient;

    @Override
    public HealthCheckResponse call() {
        return HealthCheckUtil.checkApi("ZEP", zepLivenessRestClient);
    }
}
