package com.gepardec.mega.zep.rest.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectDetail;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.faulttolerance.api.RateLimitException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Path("/projects")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@RateLimit(value = 1000, window = 5, windowUnit = ChronoUnit.MINUTES)
@Retry(delay = 1000, retryOn = RateLimitException.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApplicationScoped
public interface ZepProjectRestClient {

    @GET
    Uni<ZepResponse<List<ZepProject>>> getProjectByStartEnd(
            @QueryParam("start_date") String startDate,
            @QueryParam("end_date") String endDate,
            @QueryParam("page") int page
    );

    @GET
    Uni<ZepResponse<List<ZepProject>>> getProjectByName(
            @QueryParam("start_date") String startDate,
            @QueryParam("end_date") String endDate,
            @QueryParam("name") String name
    );

    @GET
    @Path("{id}")
    Uni<ZepResponse<ZepProjectDetail>> getProjectById(@PathParam("id") int projectId);

    @GET
    @Path("{id}/employees")
    Uni<ZepResponse<List<ZepProjectEmployee>>> getProjectEmployees(
            @PathParam("id") int projectId,
            @QueryParam("page") int page
    );
}
