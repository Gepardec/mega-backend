package com.gepardec.mega.rest.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/config")
@Tag(name = "ConfigResource")
public interface ConfigResource {
    @Path("/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response get();
}
