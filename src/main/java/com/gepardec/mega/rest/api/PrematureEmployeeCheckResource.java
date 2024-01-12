package com.gepardec.mega.rest.api;

import com.gepardec.mega.rest.model.PrematureEmployeeCheckDto;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/prematureemployeecheck")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PrematureEmployeeCheckResource {

    @POST
    Response add(@NotNull(message = "{stepEntryResource.parameter.notNull}") PrematureEmployeeCheckDto prematureEmployeeCheckDto);

    @PUT
    Response update(@NotNull(message = "{stepEntryResource.parameter.notNull}") PrematureEmployeeCheckDto prematureEmployeeCheckDto);
}
