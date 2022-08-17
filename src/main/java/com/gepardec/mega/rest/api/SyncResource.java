package com.gepardec.mega.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.YearMonth;

@Path("/sync")
public interface SyncResource {

    @Path("/projects")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response syncProjects(@QueryParam("from") YearMonth from, @QueryParam("to") YearMonth to);

    @Path("/employees")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response syncEmployees();

    @Path("/enterprise-entries")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response generateEnterpriseEntries();


    @Path("/step-entries")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response generateStepEntries();
}
