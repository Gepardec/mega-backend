package com.gepardec.mega.zep.rest.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.application.configuration.ZepConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.LocalDate;

@Path("/projects")
@RegisterRestClient(configKey = "zep")
@ClientHeaderParam(name = "Authorization", value = "{getAuthHeaderValue}")
@JsonIgnoreProperties(ignoreUnknown = true)
@ApplicationScoped
public interface ZepProjectRestClient extends Authenticatable{

    @GET
    Response getProjects(@QueryParam("page") int page);

    @GET
    Response getProjectByStartEnd(@QueryParam("start_date") LocalDate startDate,
                                  @QueryParam("end_date") LocalDate endDate,
                                  @QueryParam("page") int page);

    @GET
    @Path("{id}/employees")
    Response getProjectEmployees(@PathParam("id") int projectId);
}
