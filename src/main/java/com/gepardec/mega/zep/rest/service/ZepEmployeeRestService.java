package com.gepardec.mega.zep.rest.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.Set;

@Path("/employees")
@RegisterRestClient //(baseUri="https://www.zep-online.de/zepgepardecservices_test/next/api/v1/")
@ClientHeaderParam(name = "Authorization", value = "Bearer {com.gepardec.mega.application.configuration.ZepConfig.getRestToken}")
@JsonIgnoreProperties(ignoreUnknown = true)
public interface ZepEmployeeRestService {
    @GET
    @Path("{username}")
    Response getByUsername(@PathParam("username") String username);
}
