package com.gepardec.mega.personio.employees;

import com.gepardec.mega.personio.commons.factory.PersonioHeadersFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/company")
@ApplicationScoped
@RegisterRestClient(configKey = "personio-api-v1")
@RegisterClientHeaders(PersonioHeadersFactory.class)
public interface PersonioEmployeesClient {

    @GET
    @Path("/employees")
    @Consumes(MediaType.APPLICATION_JSON)
    EmployeesResponse getByEmail(@QueryParam("email") String email);
}
