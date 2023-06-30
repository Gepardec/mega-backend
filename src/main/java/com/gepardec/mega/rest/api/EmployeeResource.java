package com.gepardec.mega.rest.api;

import com.gepardec.mega.rest.model.EmployeeDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/employees")
public interface EmployeeResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response list();

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response update(@NotEmpty(message = "{workerResource.employees.notEmpty}") List<EmployeeDto> employeesDto);
}
