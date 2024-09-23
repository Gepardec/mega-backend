package com.gepardec.mega.rest.api;

import io.quarkus.oidc.Tenant;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.LocalDateTime;

@Tenant("pubsub")
@Path("/pubsub")
public interface PubSubResource {

    /**
     * This endpoint serves as a webhook for new emails from ZEP to trigger comment creation.
     * A Google Cloud Pub/Sub subscription is set up to call this endpoint when a new email is received.
     *
     * @return
     */
    @Operation(operationId = "gmailMessageReceivedWebhook", description = "Webhook for new emails from ZEP to trigger comment creation.")
    @POST
    @Path("/message-received")
    Response gmailMessageReceivedWebhook(String payload);

    @Path("/ping")
    @POST
    LocalDateTime ping(String payload);
}
