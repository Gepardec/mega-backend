package com.gepardec.mega.rest.api;

import com.gepardec.mega.rest.model.BillDto;
import com.gepardec.mega.rest.model.EmployeeDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Path("/employees")
@Tag(name = "EmployeeResource")
@Produces(MediaType.APPLICATION_JSON)
public interface EmployeeResource {
    @GET
    Response list();

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response update(@NotEmpty(message = "{workerResource.employees.notEmpty}") List<EmployeeDto> employeesDto);


}
