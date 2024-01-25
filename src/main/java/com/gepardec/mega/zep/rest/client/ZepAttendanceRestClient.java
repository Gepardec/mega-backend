package com.gepardec.mega.zep.rest.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.application.configuration.ZepConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/attendances")
@RegisterRestClient
@ClientHeaderParam(name = "Authorization", value = "{getAuthHeaderValue}")
@JsonIgnoreProperties(ignoreUnknown = true)
@ApplicationScoped
public interface ZepAttendanceRestClient {

    @GET
    Response getAttendancesByUsername(@QueryParam("employee_id") String username, @QueryParam("page") int page);
    static String getAuthHeaderValue() {
        return "Bearer " + ZepConfig.getRestBearerToken();
    }
}
