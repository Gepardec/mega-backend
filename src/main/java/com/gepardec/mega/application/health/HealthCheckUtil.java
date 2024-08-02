package com.gepardec.mega.application.health;

import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.health.HealthCheckResponse;

public class HealthCheckUtil {

    private HealthCheckUtil() {
    }

    public static HealthCheckResponse checkApi(String serviceName, LivenessApi api) {
        return HealthCheckResponse.named(serviceName)
                .status(isUp(api))
                .build();
    }

    private static boolean isUp(LivenessApi api) {
        return getApiStatus(api) == HttpStatus.SC_OK;
    }

    private static int getApiStatus(LivenessApi api) {
        try (Response response = api.liveness()) {
            return response.getStatus();
        }
    }
}
