package com.gepardec.mega.personio;

import com.gepardec.mega.personio.commons.factory.PersonioHeadersFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@ApplicationScoped
@RegisterRestClient(configKey = "personio-health")
@RegisterClientHeaders( PersonioHeadersFactory.class)
public interface PersonioHealthClient {

    @GET
    @Path("/")
    Response health();
}
