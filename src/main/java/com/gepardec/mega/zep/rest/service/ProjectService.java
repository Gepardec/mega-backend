package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.dto.ZepApiResponse;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectDetail;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.util.PaginationHelper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProjectService {

    @Inject
    @RestClient
    ZepProjectRestClient zepProjectRestClient;

    @Inject
    PaginationHelper paginationHelper;

    @Inject
    Logger logger;

    public List<ZepProject> getProjectsForMonthYear(YearMonth month) {
        String start = month.atDay(1).toString();
        String end   = month.atEndOfMonth().toString();

        return paginationHelper
                .fetchAllPages(page ->
                        zepProjectRestClient.getProjectByStartEnd(start, end, page)
                )
                .collect().asList()
                .await().indefinitely();
    }


    public Optional<ZepProject> getProjectByName(String name, YearMonth payrollMonth) {
        String startDate = payrollMonth.atDay(1).toString();
        String endDate = payrollMonth.atEndOfMonth().toString();
        if (name == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(paginationHelper.fetchAllPages(page -> zepProjectRestClient.getProjectByName(startDate, endDate, name))
                    .collect().asList()
                    .await().indefinitely()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ZepServiceException("No project found with name: " + name)));
        } catch (ZepServiceException e) {

            String message = "Error retrieving project \"%s\" from ZEP: No /data field in response".formatted(name);
            logger.warn(message, e);
        }
        return Optional.empty();
    }

    public Optional<ZepProjectDetail> getProjectById(int projectId) {
        try {
            Uni<ZepApiResponse<ZepProjectDetail>> uni = zepProjectRestClient.getProjectById(projectId);

            ZepApiResponse<ZepProjectDetail> resp = uni
                    .await()
                    .indefinitely();

            List<ZepProjectDetail> data = resp.data();
            if (data != null && !data.isEmpty()) {
                return Optional.of(data.get(0));
            } else {
                logger.warn("No data returned for projectId=" + projectId);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.warn("Error retrieving projectId=" + projectId + " from ZEP", e);
            return Optional.empty();
        }
    }

    public List<ZepProjectEmployee> getProjectEmployeesForId(int projectId) {
        try {

            return paginationHelper.fetchAllPages(
                    page -> zepProjectRestClient.getProjectEmployees(projectId, page)
            ).collect().asList().await().indefinitely();
        } catch (ZepServiceException e) {
            String message = "Error retrieving project employees for project \"%d\" from ZEP".formatted(projectId);
            logger.warn(message, e);
        }
        return List.of();
    }
}
