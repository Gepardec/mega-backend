package com.gepardec.mega.application.health;

import com.gepardec.mega.personio.PersonioLivenessClient;
import io.quarkus.cache.CacheResult;
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
    @CacheResult(cacheName = "personio-liveness")
    public HealthCheckResponse call() {
        return HealthCheckUtil.checkApi("Personio", personioLivenessClient);
    }
}
