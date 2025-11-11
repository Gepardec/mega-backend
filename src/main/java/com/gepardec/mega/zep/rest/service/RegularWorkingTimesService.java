package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.util.ResponseParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.util.List;

@ApplicationScoped
public class RegularWorkingTimesService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    Logger logger;

    @Inject
    ResponseParser responseParser;

    public List<ZepRegularWorkingTimes> getRegularWorkingTimesByUsername(String username) {
        try {
            return responseParser.retrieveAll(
                    page -> zepEmployeeRestClient.getRegularWorkingTimesByUsername(username, page),
                    ZepRegularWorkingTimes.class);
        } catch (ZepServiceException e) {
            String message = "Error retrieving regular working times for employee \"%s\" from ZEP: No /data field in response".formatted(username);
            logger.warn(message, e);
        }

        return List.of();
    }
}
