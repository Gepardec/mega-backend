package com.gepardec.mega.zep.rest.client;

import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/attendances")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@ClientQueryParam(name = "limit", value = "${mega.zep.page-size}")
@ApplicationScoped
public interface ZepAttendanceRestClient {

    @GET
    Uni<ZepResponse<List<ZepAttendance>>> getAttendance(
            @QueryParam("start_date") String startDate,
            @QueryParam("end_date") String endDate,
            @QueryParam("employee_id") String username,
            @QueryParam("page") int page
    );

    @GET
    Uni<ZepResponse<List<ZepAttendance>>> getAttendanceForUserAndProject(
            @QueryParam("start_date") String startDate,
            @QueryParam("end_date") String endDate,
            @QueryParam("employee_id") String username,
            @QueryParam("project_id") Integer projectId,
            @QueryParam("page") int page
    );

    @GET
    Uni<ZepResponse<List<ZepAttendance>>> getAttendanceForProject(
            @QueryParam("start_date") String startDate,
            @QueryParam("end_date") String endDate,
            @QueryParam("project_id") Integer projectId,
            @QueryParam("page") int page
    );
}
