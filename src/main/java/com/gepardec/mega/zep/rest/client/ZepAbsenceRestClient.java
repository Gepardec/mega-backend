package com.gepardec.mega.zep.rest.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.faulttolerance.api.RateLimitException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.temporal.ChronoUnit;

@Path("/absences")
@RegisterRestClient(configKey = "zep")
@RegisterClientHeaders(AuthHeaders.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@RateLimit(value = 1000, window = 5, windowUnit = ChronoUnit.MINUTES, type = RateLimitType.ROLLING)
@Retry(delay = 1000, retryOn = RateLimitException.class)
@ApplicationScoped
public interface ZepAbsenceRestClient {

    @GET
    @Path("{id}")
    Uni<ZepResponse<ZepAbsence>> getAbsenceById(@PathParam("id") Integer id);
}
