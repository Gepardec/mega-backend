package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectDetail;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.util.ResponseParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProjectService {

    @RestClient
    ZepProjectRestClient zepProjectRestClient;

    @Inject
    Logger logger;

    @Inject
    ResponseParser responseParser;

    public List<ZepProject> getProjectsForMonthYear(YearMonth payrollMonth) {
        String startDate = payrollMonth.atDay(1).toString();
        String endDate = payrollMonth.atEndOfMonth().toString();

        try {
            return responseParser.retrieveAll(
                    page -> zepProjectRestClient.getProjectByStartEnd(startDate, endDate, page),
                    ZepProject.class
            );
        } catch (ZepServiceException e) {
            String message = "Error retrieving projects for month \"%s\" from ZEP: No /data field in response".formatted(payrollMonth);
            logger.warn(message, e);
        }

        return List.of();
    }


    public Optional<ZepProject> getProjectByName(String name, YearMonth payrollMonth) {
        String startDate = payrollMonth.atDay(1).toString();
        String endDate = payrollMonth.atEndOfMonth().toString();
        if (name == null) {
            return Optional.empty();
        }
        try {
            return responseParser.retrieveSingle(
                    zepProjectRestClient.getProjectByName(startDate, endDate, name),
                    ZepProject[].class
            ).map(x -> x[0]);
        } catch (ZepServiceException e) {
            String message = "Error retrieving project \"%s\" from ZEP: No /data field in response".formatted(name);
            logger.warn(message, e);
        }
        return Optional.empty();
    }

    public Optional<ZepProjectDetail> getProjectById(int projectId) {
        try {
            return responseParser.retrieveSingle(
                    zepProjectRestClient.getProjectById(projectId),
                    ZepProjectDetail.class
            );
        } catch (ZepServiceException e) {
            String message = "Error retrieving project \"%d\" from ZEP: No /data field in response".formatted(projectId);
            logger.warn(message, e);
        }
        return Optional.empty();
    }


    public List<ZepProjectEmployee> getProjectEmployeesForId(int projectId) {
        try {
            return responseParser.retrieveAll(
                    page -> zepProjectRestClient.getProjectEmployees(projectId, page),
                    ZepProjectEmployee.class
            );
        } catch (ZepServiceException e) {
            String message = "Error retrieving project employees for project \"%d\" from ZEP: No /data field in response".formatted(projectId);
            logger.warn(message, e);
        }

        return List.of();
    }
}
