package com.gepardec.mega.rest.api;

import com.gepardec.mega.rest.model.EmployeeStepDto;
import com.gepardec.mega.rest.model.ProjectStepDto;
import com.gepardec.mega.rest.model.UpdateEmployeeStepDto;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/stepentry")
@Tag(name = "StepEntryResource")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface StepEntryResource {
    @PUT
    @Path("/close")
    Response close(@NotNull(message = "{stepEntryResource.parameter.notNull}") EmployeeStepDto employeeStepDto);

    @PUT
    @Path("/updateEmployeeStateForOffice")
    Response updateEmployeeStateForOffice(@NotNull(message = "{stepEntryResource.parameter.notNull}") UpdateEmployeeStepDto updateEmployeeStepDto);

    @PUT
    @Path("/updateEmployeeStateForProject")
    Response updateEmployeeStateForProject(@NotNull(message = "{stepEntryResource.parameter.notNull}") ProjectStepDto projectStepDto);
}
