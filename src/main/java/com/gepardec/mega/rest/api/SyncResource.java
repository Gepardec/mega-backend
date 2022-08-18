package com.gepardec.mega.rest.api;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.YearMonth;

@Path("/sync")
@Tag(name = "Sync for testing")
@Produces(MediaType.APPLICATION_JSON)
public interface SyncResource {

    @Parameter(name = "from",
            description = "does something",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING, example = "yyyy-MM"))
    @Path("/projects")
    @GET
    Response syncProjects(@QueryParam("from") YearMonth from, @QueryParam("to") YearMonth to);

    @Path("/employees")
    @GET
    Response syncEmployees();

    @Path("/enterprise-entries")
    @GET
    Response generateEnterpriseEntries(@QueryParam("from") YearMonth from, @QueryParam("to") YearMonth to);

    @Path("/step-entries")
    @GET
    Response generateStepEntries(@QueryParam("from") YearMonth from, @QueryParam("to") YearMonth to);

    @Path("/all")
    @GET
    Response syncAll(@QueryParam("from") YearMonth from, @QueryParam("to") YearMonth to);
}
