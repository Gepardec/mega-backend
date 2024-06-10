package com.gepardec.mega.zep.rest.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@RegisterRestClient(configKey = "zep-health")
@RegisterClientHeaders(AuthHeaders.class)
@ApplicationScoped
public interface ZepHealthRestClient {

    @GET
    @Path("/")
    Response health();

}
