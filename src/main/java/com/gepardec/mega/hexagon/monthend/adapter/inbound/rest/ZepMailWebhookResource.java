package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateClarificationFromZepMailUseCase;
import io.quarkus.logging.Log;
import io.quarkus.oidc.Tenant;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.LocalDateTime;

@RequestScoped
@Authenticated
@Tenant("pubsub")
@Path("/pubsub")
public class ZepMailWebhookResource {

    private final CreateClarificationFromZepMailUseCase createClarificationFromZepMailUseCase;

    @Inject
    public ZepMailWebhookResource(CreateClarificationFromZepMailUseCase createClarificationFromZepMailUseCase) {
        this.createClarificationFromZepMailUseCase = createClarificationFromZepMailUseCase;
    }

    @POST
    @Path("/message-received")
    @Operation(operationId = "gmailMessageReceivedWebhook", description = "Webhook for new emails from ZEP to trigger comment creation.")
    public Response gmailMessageReceivedWebhook(String payload) {
        try {
            logNotificationReceived(payload);
            createClarificationFromZepMailUseCase.create();
            return Response.ok().build();
        } catch (Exception exception) {
            Log.error("Unhandled exception while processing ZEP mail webhook", exception);
            return Response.serverError().entity(exception.getMessage()).build();
        }
    }

    @POST
    @Path("/ping")
    public LocalDateTime ping(String payload) {
        logNotificationReceived(payload);
        return LocalDateTime.now();
    }

    private void logNotificationReceived(String payload) {
        Log.infof("Received notification from Pub/Sub: %s", payload != null ? payload.strip() : null);
    }
}
