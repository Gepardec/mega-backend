package com.gepardec.mega.rest.api;
import com.gepardec.mega.rest.model.BulkUpdateDto;
import com.gepardec.mega.rest.model.EmployeeDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

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

    @POST
    @Path("/bulkUpdate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response uploadInternalRate(@MultipartForm BulkUpdateDto input, @Context HttpHeaders headers);

    @GET
    @Path("/csvTemplate")
    @Produces("text/csv")
    Response downloadCsvTemplate();

}
