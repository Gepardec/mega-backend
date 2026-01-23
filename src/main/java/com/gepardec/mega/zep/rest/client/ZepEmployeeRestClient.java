package com.gepardec.mega.zep.rest.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import com.gepardec.mega.zep.rest.dto.ZepEmploymentPeriod;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
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


@Path("/employees")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@RateLimit(value = 1000, window = 5, windowUnit = ChronoUnit.MINUTES)
@Retry(delay = 1000, retryOn = RateLimitException.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApplicationScoped
public interface ZepEmployeeRestClient {

    @GET
    @Path("/{username}")
    Uni<ZepResponse<ZepEmployee>> getByUsername(@PathParam("username") String username);

    @GET
    @Path("/{username}/employment-periods")
    Uni<ZepResponse<List<ZepEmploymentPeriod>>> getEmploymentPeriodByUserName(
            @PathParam("username") String username,
            @QueryParam("page") Integer page
    );

    @GET
    @Path("/{username}/regular-working-times")
    Uni<ZepResponse<List<ZepRegularWorkingTimes>>> getRegularWorkingTimesByUsername(
            @PathParam("username") String username,
            @QueryParam("page") Integer page
    );

    @GET
    Uni<ZepResponse<List<ZepEmployee>>> getAllEmployeesOfPage(@QueryParam("page") int page);

    @GET
    @Path("/{username}/absences")
    Uni<ZepResponse<List<ZepAbsence>>> getAbsencesByUsername(
            @PathParam("username") String username,
            @QueryParam("page") int page
    );
}
