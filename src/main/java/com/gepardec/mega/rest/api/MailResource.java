package com.gepardec.mega.rest.api;

import io.quarkus.oidc.Tenant;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;

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

    @Operation(operationId = "send-reminder", description = "Sends reminder emails to affected employees.")
    @GET
    @Path("/send-reminder")
    Response sendReminder();

    /**
     * The sole purpose of this endpoint is to trigger the retrieval of emails from the ZEP inbox manually.
     * This is useful for testing purposes.
     * Therefore, this endpoint must not be used in production!
     *
     * @return
     */
    @Operation(operationId = "retrieve-zep-mails", description = "Trigger email retrieval from mail inbox manually.")
    @GET
    @Path("/retrieve-zep-mails")
    Response retrieveZepEmailsFromInbox();

    @Path("/ping")
    @GET
    LocalDateTime ping();
}
