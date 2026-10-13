package com.gepardec.mega.zep.rest.client;

import com.gepardec.mega.zep.rest.dto.ZepReceipt;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAttachment;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/receipts")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@ClientQueryParam(name = "limit", value = "${mega.zep.page-size}")
@ApplicationScoped
public interface ZepReceiptRestClient {
    @GET
    Uni<ZepResponse<List<ZepReceipt>>> getAllReceiptsForMonth(
            @QueryParam("start_date") String startDate,
            @QueryParam("end_date") String endDate,
            @QueryParam("page") int page
    );

    @GET
    @Path("{id}/attachments")
    Uni<ZepResponse<ZepReceiptAttachment>> getAttachmentForReceipt(@PathParam("id") int receiptId);
}
