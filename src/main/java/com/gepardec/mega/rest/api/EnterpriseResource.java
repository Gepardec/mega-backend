package com.gepardec.mega.rest.api;

import com.gepardec.mega.rest.model.EnterpriseEntryDto;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/enterprise")
public interface EnterpriseResource {
    @GET
    @Path("/entriesformonthyear/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getEnterpriseEntryForMonthYear(@PathParam("year") Integer year, @PathParam("month") Integer month);

    @PUT
    @Path("/entry/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateEnterpriseEntry(@PathParam("year") Integer year, @PathParam("month") Integer month, @RequestBody EnterpriseEntryDto entryDto);
}
