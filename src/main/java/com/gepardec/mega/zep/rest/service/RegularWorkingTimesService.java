package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.util.ResponseParser;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class RegularWorkingTimesService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    Logger logger;

    @Inject
    ResponseParser responseParser;

    @RateLimit(value = 6, window = 1, windowUnit = ChronoUnit.SECONDS)
    public List<ZepRegularWorkingTimes> getRegularWorkingTimesByUsername(String username) {
        try  {
            List<ZepRegularWorkingTimes> zepRegularWorkingTimes = responseParser.retrieveAll(
                    page -> zepEmployeeRestClient.getRegularWorkingTimesByUsername(username, page),
                    ZepRegularWorkingTimes.class);
        } catch (ZepServiceException e) {
            logger.warn("Error retrieving regular working times for employee \"%s\" from ZEP: No /data field in response"
                    .formatted(username), e);
        }

        return List.of();
    }
}
