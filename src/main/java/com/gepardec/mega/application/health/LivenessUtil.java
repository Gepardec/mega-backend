package com.gepardec.mega.application.health;

import com.gepardec.mega.db.entity.common.ThirdPartyType;
import com.gepardec.mega.personio.PersonioHealthClient;
import com.gepardec.mega.zep.rest.client.ZepHealthRestClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class LivenessUtil {
    @Inject
    @RestClient
    PersonioHealthClient personioHealthClient;

    @Inject
    @RestClient
    ZepHealthRestClient zepHealthRestClient;

    public HealthCheckResponse getResponseForPersonio() {
        return getResponseForService(ThirdPartyType.PERSONIO.getName());
    }

    public HealthCheckResponse getResponseForZep() {
        return getResponseForService(ThirdPartyType.ZEP.getName());
    }

    private HealthCheckResponse getResponseForService(String serviceName) {
        HealthCheckResponseBuilder responseBuilder;
        int statusCode;

        if(serviceName.equalsIgnoreCase(ThirdPartyType.PERSONIO.getName())) {
            responseBuilder = HealthCheckResponse.named(String.format("%s Liveness", ThirdPartyType.PERSONIO.getName()));
            try (Response response = personioHealthClient.health()) {
                statusCode = response.getStatus();
            }
        } else {
            responseBuilder = HealthCheckResponse.named(String.format("%s Liveness", ThirdPartyType.ZEP.getName()));
            try (Response response = zepHealthRestClient.health()) {
                statusCode = response.getStatus();
            }
        }
        responseBuilder.status(statusCode == Response.Status.OK.getStatusCode());
        return responseBuilder.build();
    }
}
