package com.gepardec.mega.rest.api;

import io.quarkus.oidc.Tenant;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.security.*;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Path("/sync")
@Tenant("mega-cron")
@Tag(name = "SyncResource")
@Produces(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "mega-cron")
@SecuritySchemes(
        @SecurityScheme(
                securitySchemeName = "mega-cron",
                type = SecuritySchemeType.OAUTH2,
                flows = @OAuthFlows(clientCredentials = @OAuthFlow())
        )
)
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


    @Operation(operationId = "syncPrematureEmployeeChecks", description = "Sync PrematureEmployeeChecks with existing StepEntries and updates these accordingly.")
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
    @Path("/prematureemployeecheck")
    @GET
    Response syncPrematureEmployeeChecks(@QueryParam("from") YearMonth from, @QueryParam("to") YearMonth to);

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

    @Path("/ping")
    @GET
    LocalDateTime ping();
}
