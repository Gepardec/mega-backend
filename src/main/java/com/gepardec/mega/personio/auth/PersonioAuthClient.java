package com.gepardec.mega.personio.auth;

import com.gepardec.mega.personio.commons.factory.PersonioClientToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/auth")
@ApplicationScoped
@RegisterRestClient(configKey = "personio-api-v1")
public interface PersonioAuthClient {

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    AuthResponse authenticate(@RequestBody PersonioClientToken personioClientToken);
}
