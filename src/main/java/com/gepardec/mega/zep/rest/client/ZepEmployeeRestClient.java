package com.gepardec.mega.zep.rest.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.application.configuration.ZepConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/employees")
@RegisterRestClient(configKey = "zep")
@ClientHeaderParam(name = "Authorization", value = "{getAuthHeaderValue}")
@JsonIgnoreProperties(ignoreUnknown = true)
@ApplicationScoped
public interface ZepEmployeeRestClient extends Authenticatable {

    @GET
    Response getByPersonalNumber(@QueryParam("personal_number") List<String> personalNumber);

    @GET
    @Path("/{username}")
    Response getByUsername(@PathParam("username") String username);

    @GET
    @Path("/{username}/employment-periods")
    Response getEmploymentPeriodByUserName(@PathParam("username") String username);

    @GET
    @Path("/{username}/regular-working-times")
    Response getRegularWorkingTimesByUsername(@PathParam("username") String username);

    @GET
    Response getAllEmployeesOfPage(@QueryParam("page") int page);

    @GET
    @Path("/{username}/absences")
    Response getAbsencesByUsername(@PathParam("username") String username);

}
