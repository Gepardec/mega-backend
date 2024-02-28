package com.gepardec.mega.zep.rest.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.LocalDate;

@Path("/employees")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApplicationScoped
public interface ZepEmployeeRestClient {

    @GET
    Response getByPersonalNumber(@QueryParam("personal_number") String personalNumber);

    @GET
    @Path("/{username}")
    Response getByUsername(@PathParam("username") String username);

    @GET
    @Path("/{username}/employment-periods")
    Response getEmploymentPeriodByUserName(@PathParam("username") String username, @QueryParam("page") Integer page);

    @GET
    @Path("/{username}/regular-working-times")
    Response getRegularWorkingTimesByUsername(@PathParam("username") String username, @QueryParam("page") Integer page);

    @GET
    Response getAllEmployeesOfPage(@QueryParam("page") int page);

    @GET
    @Path("/{username}/absences")
    Response getAbsencesByUsername(@PathParam("username") String username,
                                   @QueryParam("start_date") LocalDate startDate,
                                   @QueryParam("end_date") LocalDate endDate,
                                   @QueryParam("page") int page);

}
