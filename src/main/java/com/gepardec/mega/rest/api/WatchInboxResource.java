package com.gepardec.mega.rest.api;

import com.google.api.services.gmail.model.WatchResponse;
import io.quarkus.oidc.Tenant;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/watch-inbox")
@Tenant("mega-cron")
@Tag(name = "WatchInboxResource")
@Produces(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "mega-cron")
@SecuritySchemes(
        @SecurityScheme(
                securitySchemeName = "mega-cron",
                type = SecuritySchemeType.OAUTH2,
                flows = @OAuthFlows(clientCredentials = @OAuthFlow())
        )
)
public interface WatchInboxResource {

    @POST
    @Path("/watch")
    @Operation(operationId = "watch", description = "Send a watch request to the Gmail API to receive notifications for new emails.")
    WatchResponse watchInbox();

    @POST
    @Path("/stop")
    @Operation(operationId = "stop", description = "Stop watching the inbox.")
    @APIResponse(responseCode = "204", description = "Watch stopped successfully")
    Void stopInbox();
}
