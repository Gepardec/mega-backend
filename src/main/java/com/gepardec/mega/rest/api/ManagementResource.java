package com.gepardec.mega.rest.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/management")
@Tag(name = "ManagementResource")
@Produces(MediaType.APPLICATION_JSON)
public interface ManagementResource {
    @GET
    @Path("/officemanagemententries/{year}/{month}")
    Response getAllOfficeManagementEntries(@PathParam("year") Integer year, @PathParam("month") Integer month, @QueryParam("projectStateLogicSingle") boolean projectStateLogicSingle);

    @GET
    @Path("/projectmanagemententries/{year}/{month}")
    Response getAllProjectManagementEntries(@PathParam("year") Integer year, @PathParam("month") Integer month, @QueryParam("all") boolean allProjects, @QueryParam("projectStateLogicSingle") boolean projectStateLogicSingle);

    @GET
    @Path("/projectsWithoutLeads")
    Response getProjectsWithoutLeads();
}
