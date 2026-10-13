package com.gepardec.mega.zep.rest.client;

import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import com.gepardec.mega.zep.rest.dto.ZepEmploymentPeriod;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;


@Path("/employees")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@ClientQueryParam(name = "limit", value = "${mega.zep.page-size}")
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
