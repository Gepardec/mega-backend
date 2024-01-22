package com.gepardec.mega.zep.rest.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.application.configuration.ZepConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/absences")
@RegisterRestClient
@ClientHeaderParam(name = "Authorization", value = "{getAuthHeaderValue}")
@JsonIgnoreProperties(ignoreUnknown = true)
@ApplicationScoped
public interface ZepAbsenceRestClient {

    @GET
    @Path("{id}")
    Response getAbsenceById(@PathParam("id") Integer id);

    static String getAuthHeaderValue() {
        return "Bearer " + ZepConfig.getRestBearerToken();
    }
}
