package com.gepardec.mega.zep.rest.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;


@Path("/employees")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApplicationScoped
public interface ZepEmployeeRestClient {

    @GET
    Uni<ZepEmployee> getByPersonalNumber(@QueryParam("personal_number") String personalNumber);

    @GET
    @Path("/{username}")
    Uni<ZepEmployee> getByUsername(@PathParam("username") String username);

    @GET
    @Path("/{username}/employment-periods")
    Response getEmploymentPeriodByUserName(@PathParam("username") String username, @QueryParam("page") Integer page);

    @GET
    @Path("/{username}/regular-working-times")
    Response getRegularWorkingTimesByUsername(@PathParam("username") String username, @QueryParam("page") Integer page);

    @GET
    Uni<List<ZepEmployee>> getAllEmployeesOfPage(@QueryParam("page") int page);

    @GET
    @Path("/{username}/absences")
    Uni<List<ZepAbsence>> getAbsencesByUsername(@PathParam("username") String username,
                                                @QueryParam("page") int page);

}
