package com.gepardec.mega.application.health;

import com.gepardec.mega.personio.PersonioLivenessClient;
import io.smallrye.health.api.Wellness;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Wellness
@ApplicationScoped
public class PersonioWellnessCheck implements HealthCheck {

    @Inject
    @RestClient
    PersonioLivenessClient personioLivenessClient;

    @Override
    public HealthCheckResponse call() {
        return HealthCheckUtil.checkApi("Personio", personioLivenessClient);
    }
}
