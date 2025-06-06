package com.gepardec.mega.zep.rest.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/attendances")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApplicationScoped
public interface ZepAttendanceRestClient {

    @GET
    Uni<ZepAttendance> getAttendance(@QueryParam("start_date") String startDate, @QueryParam("end_date") String endDate, @QueryParam("employee_id") String username, @QueryParam("page") int page);

    @GET
    Uni<List<ZepAttendance>> getAttendanceForUserAndProject(@QueryParam("start_date") String startDate, @QueryParam("end_date") String endDate, @QueryParam("employee_id") String username, @QueryParam("project_id") Integer projectId, @QueryParam("page") int page);

    @GET
    Response getAttendancesByUsername(@QueryParam("employee_id") String username, @QueryParam("page") int page);

    @GET
    Response getAttendanceById(@QueryParam("id") int id);
}
