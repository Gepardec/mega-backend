package com.gepardec.mega.rest.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/config")
public interface ConfigResource {
    @Path("/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response get();
}
