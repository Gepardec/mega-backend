package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectDetail;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.util.ResponseParser;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

            // Response rawJson = zepProjectRestClient.getProjectByStartEnd(startDate, endDate, 0).await().indefinitely();


            Response rawJson =  zepProjectRestClient.getProjectByStartEnd(startDate, endDate, 0);
            System.out.println("RAW JSON: " + rawJson.getStatus() + " " + rawJson.getHeaders());
            System.out.println("Body "+rawJson.readEntity(String.class));
            //String text = zepProjectRestClient.getProjectByStartEnd(startDate, endDate, 0).await().indefinitely();

            //System.out.println(text);

            /*zepProjectRestClient.getProjectByStartEnd(startDate, endDate, 0)
                    .subscribe().with(response -> {
                        System.out.println("ZEP response: " + response);
                    }, failure -> {
                        logger.error("Failed to fetch projects", failure);
                    });


             */

        } catch (
                ZepServiceException e) {
            logger.warn("Error retrieving projects for month + \"%s\" from ZEP: No /data field in response"
                    .formatted(payrollMonth.format(DateTimeFormatter.ofPattern("MM-yyyy"))), e);
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
            logger.warn("Error retrieving project + \"%s\" from ZEP: No /data field in response"
                    .formatted(name), e);
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
            logger.warn("Error retrieving project + \"%d\" from ZEP: No /data field in response"
                    .formatted(projectId), e);
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
            logger.warn("Error retrieving project employees for project + \"%d\" from ZEP: No /data field in response"
                    .formatted(projectId), e);
        }

        return List.of();
    }
}
