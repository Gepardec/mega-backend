package com.gepardec.mega.zep.rest.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.Set;

@Path("/employees")
@RegisterRestClient
@JsonIgnoreProperties(ignoreUnknown = true)
public interface ZepEmployeeRestService {
    @GET
    @Path("{username}")
    Set<ZepEmployee> getByUsername(@PathParam("username") String username);
}
