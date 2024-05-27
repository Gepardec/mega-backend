package com.gepardec.mega.zep.rest.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/attendances")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApplicationScoped
public interface ZepAttendanceRestClient {

    @GET
    Response getAttendance(@QueryParam("start_date") String startDate, @QueryParam("end_date") String endDate, @QueryParam("employee_id") String username, @QueryParam("page") int page);

    @GET
    Response getAttendancesByUsername(@QueryParam("employee_id") String username, @QueryParam("page") int page);

    @GET
    Response getAttendanceById(@QueryParam("id") int id);
}
