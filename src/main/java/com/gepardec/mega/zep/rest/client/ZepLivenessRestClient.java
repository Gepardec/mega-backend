package com.gepardec.mega.zep.rest.client;

import com.gepardec.mega.application.health.LivenessApi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@RegisterRestClient(configKey = "zep-health")
@RegisterClientHeaders(AuthHeaders.class)
@ApplicationScoped
public interface ZepLivenessRestClient extends LivenessApi {

    @GET
    @Path("/")
    @Override
    Response liveness();
}
