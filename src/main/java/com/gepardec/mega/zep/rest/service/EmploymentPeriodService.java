package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepEmploymentPeriod;
import com.gepardec.mega.zep.util.ResponseParser;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class EmploymentPeriodService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestService;

    @Inject
    Logger logger;

    @Inject
    ResponseParser responseParser;

    @RateLimit(value = 6, window = 1, windowUnit = ChronoUnit.SECONDS)
    public List<ZepEmploymentPeriod> getZepEmploymentPeriodsByUsername(String username) {
        try {
            return responseParser.retrieveAll(
                    page -> zepEmployeeRestService.getEmploymentPeriodByUserName(username, page),
                    ZepEmploymentPeriod.class
            );
        } catch (ZepServiceException e) {
            logger.warn("Error retrieving employment periods for employee \"%s\" from ZEP: No /data field in response"
                    .formatted(username), e);
        }

        return List.of();
    }

}
