package com.gepardec.mega.zep.rest.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/receipts")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApplicationScoped
public interface ZepReceiptRestClient {
    @GET
    Response getAllReceiptsForMonth(@QueryParam("start_date") String startDate,
                                    @QueryParam("end_date") String endDate,
                                    @QueryParam("page") int page);

    @GET
    @Path("{id}/attachments")
    Response getAttachmentForReceipt(@PathParam("id") int receiptId, @QueryParam("page") int page);
}
