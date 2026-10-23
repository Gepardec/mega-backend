package com.gepardec.mega.zep.rest.client;

import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import com.gepardec.mega.zep.rest.dto.ZepEmployeeReleaseDateUpdate;
import com.gepardec.mega.zep.rest.dto.ZepEmploymentPeriod;
import com.gepardec.mega.zep.rest.dto.ZepMeal;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
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

    @PATCH
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Void> updateReleaseDate(@PathParam("username") String username, ZepEmployeeReleaseDateUpdate body);

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

    @GET
    @Path("/{username}/meals")
    Uni<ZepResponse<List<ZepMeal>>> getMealsByUsername(
            @PathParam("username") String username,
            @QueryParam("page") int page
    );
}
