package com.gepardec.mega.rest.api;


import com.gepardec.mega.rest.model.HourlyRateFileDto;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;


@Path("/bulkupdate")
@Tag(name = "BulkUpdateResource")
public interface BulkUpdateResource {

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response uploadHourlyRate(@MultipartForm HourlyRateFileDto input);

}
