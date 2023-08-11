package com.gepardec.mega.personio.client;

import com.gepardec.mega.personio.factory.PersonioHeadersFactory;
import com.gepardec.mega.personio.model.EmployeesResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/company")
@RegisterRestClient(configKey = "personio-api-v1")
@RegisterClientHeaders(PersonioHeadersFactory.class)
public interface EmployeesClient {

    @GET
    @Path("/employees")
    @Consumes(MediaType.APPLICATION_JSON)
    EmployeesResponse getByEmail(@QueryParam("email") String email);
}
