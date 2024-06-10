package com.gepardec.mega.application.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public class ZepLivenessCheck implements HealthCheck {

    @Inject
    LivenessUtil livenessUtil;

    @Override
    public HealthCheckResponse call() {
        return livenessUtil.getResponseForZep();
    }

}
