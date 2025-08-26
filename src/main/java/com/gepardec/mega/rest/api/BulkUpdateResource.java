package com.gepardec.mega.rest.api;


import com.gepardec.mega.rest.model.HourlyRateFileDto;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;


@Path("/employees")
@Tag(name = "EmployeeResource")
public interface BulkUpdateResource {
    @POST
    @Path("/bulkUpdate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    Response uploadInternalRate(@MultipartForm HourlyRateFileDto input, @Context HttpHeaders headers);
}
