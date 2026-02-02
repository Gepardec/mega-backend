package com.gepardec.mega.zep.rest.client;

import com.gepardec.mega.zep.rest.dto.ZepReceipt;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAttachment;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.faulttolerance.api.RateLimitException;
import io.smallrye.faulttolerance.api.RateLimitType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Path("/receipts")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@RateLimit(value = 1000, window = 5, windowUnit = ChronoUnit.MINUTES, type = RateLimitType.ROLLING)
@Retry(delay = 1000, retryOn = RateLimitException.class)
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
