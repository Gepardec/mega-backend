package com.gepardec.mega.personio.client;

import com.gepardec.mega.personio.PersonioHeadersFactory;
import com.gepardec.mega.personio.model.EmployeesResponse;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/company")
@RegisterRestClient(configKey="employees-api")
@RegisterClientHeaders(PersonioHeadersFactory.class)
public interface EmployeesClient {
    @GET
    @Path("/employees")
    EmployeesResponse getEmployeeByEmail(@QueryParam("email") String email);
}
