package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepEmploymentPeriod;
import com.gepardec.mega.zep.util.ResponseParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.util.List;

@ApplicationScoped
public class EmploymentPeriodService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestService;

    @Inject
    Logger logger;

    @Inject
    ResponseParser responseParser;

    public List<ZepEmploymentPeriod> getZepEmploymentPeriodsByEmployeeName(String employeeName) {
        try {
            return responseParser.retrieveAll(
                    page -> zepEmployeeRestService.getEmploymentPeriodByUserName(employeeName, page),
                    ZepEmploymentPeriod.class
            );
        }  catch (ZepServiceException e) {
            logger.warn("Error retrieving employment periods for employee \"%s\" from ZEP: No /data field in response"
                    .formatted(employeeName), e);
        }

        return List.of();
    }

}
