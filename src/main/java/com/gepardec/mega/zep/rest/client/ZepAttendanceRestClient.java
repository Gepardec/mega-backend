package com.gepardec.mega.zep.rest.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.faulttolerance.api.RateLimitException;
import io.smallrye.faulttolerance.api.RateLimitType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Path("/attendances")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@RateLimit(value = 1000, window = 5, windowUnit = ChronoUnit.MINUTES, type = RateLimitType.ROLLING)
@Retry(delay = 1000, retryOn = RateLimitException.class)
@JsonIgnoreProperties(ignoreUnknown = true)
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
}
