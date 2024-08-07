package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.util.ResponseParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RegularWorkingTimesService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    Logger logger;

    @Inject
    ResponseParser responseParser;

    public Optional<ZepRegularWorkingTimes> getRegularWorkingTimesByUsername(String username) {
        try  {
            List<ZepRegularWorkingTimes> zepRegularWorkingTimes = responseParser.retrieveAll(
                    page -> zepEmployeeRestClient.getRegularWorkingTimesByUsername(username, page),
                    ZepRegularWorkingTimes.class);

            if (zepRegularWorkingTimes.isEmpty()) {
                return Optional.empty();
            }
            return zepRegularWorkingTimes.stream()
                    .min(Comparator.comparing(ZepRegularWorkingTimes::startDate,
                            Comparator.nullsLast(Comparator.reverseOrder())));
        } catch (ZepServiceException e) {
            logger.warn("Error retrieving regular working times for employee \"%s\" from ZEP: No /data field in response"
                    .formatted(username), e);
        }

        return Optional.empty();
    }
}
