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

@Path("/projects")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApplicationScoped
public interface ZepProjectRestClient {

    @GET
    Response getProjects(@QueryParam("page") int page);

    @GET
    Response getProjectByStartEnd(@QueryParam("start_date") String startDate,
                                  @QueryParam("end_date") String endDate,
                                  @QueryParam("page") int page);

    @GET
    Response getProjectByName(@QueryParam("start_date") String startDate,
                              @QueryParam("end_date") String endDate,
                              @QueryParam("name") String name);

    @GET
    @Path("{id}")
    Response getProjectById(@PathParam("id") int projectId);

    @GET
    @Path("{id}/employees")
    Response getProjectEmployees(@PathParam("id") int projectId, @QueryParam("page") int page);

    @GET
    Response getProjectsByName(@QueryParam("name") String name);
}
