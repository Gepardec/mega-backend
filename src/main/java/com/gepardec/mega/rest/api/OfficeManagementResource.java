package com.gepardec.mega.rest.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.YearMonth;

@Path("/officemanagement")
@Tag(name = "OfficeManagementResource")
@Produces(MediaType.APPLICATION_JSON)
public interface OfficeManagementResource {

    @GET
    @Path("/officemanagemententries")
    Response getAllOfficeManagementEntries();

    @GET
    @Path("/officemanagemententries/{payrollMonth}")
    Response getAllOfficeManagementEntries(@PathParam("payrollMonth") YearMonth payrollMonth);

    @GET
    @Path("/projectOverview/{payrollMonth}")
    Response getProjectOverview(@PathParam("payrollMonth") YearMonth payrollMonth);

    @GET
    @Path("/projectsWithoutLeads")
    Response getProjectsWithoutLeads();
}
