package com.gepardec.mega.rest.api;

import org.eclipse.microprofile.openapi.annotations.Operation;
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
@Tag(name = "SyncResource")
@Produces(MediaType.APPLICATION_JSON)
public interface SyncResource {

    @Operation(operationId = "syncProjects", description = "Syncs projects for a given amount of months.")
    @Parameter(name = "from",
            description = "If not given uses the current month. " +
                    "If given uses the parameter-date.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING, example = "yyyy-MM"))
    @Parameter(name = "to",
            description = "If not given uses the current month. " +
                    "If 'from' is not given but 'to' is given, uses the current month. " +
                    "If both are given uses the span between them.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING, example = "yyyy-MM"))
    @Path("/projects")
    @GET
    Response syncProjects(@QueryParam("from") YearMonth from, @QueryParam("to") YearMonth to);

    @Operation(operationId = "syncEmployees", description = "Syncs current active employees.")
    @Path("/employees")
    @GET
    Response syncEmployees();

    @Operation(operationId = "generateEnterpriseEntries", description = "Generates enterprise-entries for existing data.")
    @Parameter(name = "from",
            description = "If not given uses the current month. " +
                    "If given uses the parameter-date.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING, example = "yyyy-MM"))
    @Parameter(name = "to",
            description = "If not given uses the current month. " +
                    "If 'from' is not given but 'to' is given, uses the current month. " +
                    "If both are given uses the span between them.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING, example = "yyyy-MM"))
    @Path("/enterprise-entries")
    @GET
    Response generateEnterpriseEntries(@QueryParam("from") YearMonth from, @QueryParam("to") YearMonth to);

    @Operation(operationId = "generateStepEntries", description = "Generates step-entries for existing data.")
    @Parameter(name = "from",
            description = "If not given uses the current month. " +
                    "If given uses the parameter-date.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING, example = "yyyy-MM"))
    @Parameter(name = "to",
            description = "If not given uses the current month. " +
                    "If 'from' is not given but 'to' is given, uses the current month. " +
                    "If both are given uses the span between them.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING, example = "yyyy-MM"))
    @Path("/step-entries")
    @GET
    Response generateStepEntries(@QueryParam("from") YearMonth from, @QueryParam("to") YearMonth to);

    @Operation(operationId = "syncAll", description = "Runs a complete sync for a given amount of months inclusive step-generation.")
    @Parameter(name = "from",
            description = "If not given uses the current month. " +
                    "If given uses the parameter-date.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING, example = "yyyy-MM"))
    @Parameter(name = "to",
            description = "If not given uses the current month. " +
                    "If 'from' is not given but 'to' is given, uses the current month. " +
                    "If both are given uses the span between them.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING, example = "yyyy-MM"))
    @Path("/all")
    @GET
    Response syncAll(@QueryParam("from") YearMonth from, @QueryParam("to") YearMonth to);
}
