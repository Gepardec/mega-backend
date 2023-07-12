package com.gepardec.mega.rest.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/worker")
public interface WorkerResource {
    @GET
    @Path("/monthendreports")
    @Produces(MediaType.APPLICATION_JSON)
    Response monthlyReport();

    @GET
    @Path("/monthendreports/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    Response monthlyReport(@PathParam("year") Integer year, @PathParam("month") Integer month);
}
