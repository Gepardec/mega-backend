package com.gepardec.mega.rest.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.YearMonth;

@Path("/projectmanagement")
@Tag(name = "ProjectManagementResource")
@Produces(MediaType.APPLICATION_JSON)
public interface ProjectManagementResource {

    @GET
    @Path("/projectmanagemententries")
    Response getAllProjectManagementEntries(@QueryParam("all") boolean allProjects, @QueryParam("projectStateLogicSingle") boolean projectStateLogicSingle);

    @GET
    @Path("/projectmanagemententries/{payrollMonth}")
    Response getAllProjectManagementEntries(@PathParam("payrollMonth") YearMonth payrollMonth, @QueryParam("all") boolean allProjects, @QueryParam("projectStateLogicSingle") boolean projectStateLogicSingle);
}
