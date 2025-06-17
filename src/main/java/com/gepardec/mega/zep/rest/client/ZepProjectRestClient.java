package com.gepardec.mega.zep.rest.client;

import com.gepardec.mega.zep.rest.dto.ZepApiResponse;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectDetail;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/projects")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@ApplicationScoped
public interface ZepProjectRestClient {

    @GET
    Response getProjects(@QueryParam("page") int page);

    @GET
    Uni<ZepApiResponse<ZepProject>> getProjectByStartEnd(@QueryParam("start_date") String startDate,
                                                         @QueryParam("end_date") String endDate,
                                                         @QueryParam("page") int page);

    @GET
    Uni<ZepApiResponse<ZepProject>> getProjectByName(@QueryParam("start_date") String startDate,
                                                     @QueryParam("end_date") String endDate,
                                                     @QueryParam("name") String name);

    @GET
    @Path("{id}")
    Uni<ZepApiResponse<ZepProjectDetail>> getProjectById(@PathParam("id") int projectId);

    @GET
    @Path("{id}/employees")
    Uni<ZepApiResponse<ZepProjectEmployee>> getProjectEmployees(@PathParam("id") int projectId, @QueryParam("page") int page);

    @GET
    Uni<ZepApiResponse<ZepProjectDetail>> getProjectsByName(@QueryParam("name") String name);
}
