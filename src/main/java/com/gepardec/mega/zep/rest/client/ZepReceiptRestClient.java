package com.gepardec.mega.zep.rest.client;

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
@ApplicationScoped
public interface ZepReceiptRestClient {
    @GET
    Response getAllReceiptsForMonth(@QueryParam("start_date") String startDate,
                                    @QueryParam("end_date") String endDate,
                                    @QueryParam("page") int page);

    @GET
    @Path("{id}/attachments")
    Response getAttachmentForReceipt(@PathParam("id") int receiptId);

    @GET
    @Path("{id}/amounts")
    Response getAmountForReceipt(@PathParam("id") int receiptId);
}
