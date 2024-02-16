package com.gepardec.mega.rest.api;

import io.quarkus.oidc.Tenant;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.*;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/mail")
@Tenant("mega-cron")
@Tag(name = "MailResource")
@Produces(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "mega-cron")
@SecuritySchemes(
        @SecurityScheme(
                securitySchemeName = "mega-cron",
                type = SecuritySchemeType.OAUTH2,
                flows = @OAuthFlows(clientCredentials = @OAuthFlow())
        )
)
public interface MailResource {

    @Operation(operationId = "sendReminder", description = "Sends reminder emails to affected employees.")
    @GET
    @Path("/sendReminder")
    Response sendReminder();

    @Operation(operationId = "retrieveZepEmailsFromInbox", description = "Webhook for new emails from ZEP to trigger comment creation.")
    @GET
    @Path("/retrieveZepEmails")
    Response retrieveZepEmailsFromInbox();
}
