package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectDetail;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class ProjectService {

    @RestClient
    ZepProjectRestClient zepProjectRestClient;

    @Inject
    Logger logger;

    public List<ZepProject> getProjectsForMonthYear(YearMonth payrollMonth) {
        String startDate = payrollMonth.atDay(1).toString();
        String endDate = payrollMonth.atEndOfMonth().toString();

        return Multi.createBy().repeating()
                .uni(AtomicInteger::new, page ->
                        zepProjectRestClient.getProjectByStartEnd(startDate, endDate, page.incrementAndGet())
                                .onFailure().invoke(ex -> logger.warn("Error retrieving projects from ZEP", ex))
                )
                .whilst(ZepResponse::hasNext)
                .map(ZepResponse::data)
                .onItem().<ZepProject>disjoint()
                .collect().asList()
                .await().indefinitely();
    }


    public Optional<ZepProject> getProjectByName(String name, YearMonth payrollMonth) {
        String startDate = payrollMonth.atDay(1).toString();
        String endDate = payrollMonth.atEndOfMonth().toString();

        if (name == null) {
            return Optional.empty();
        }

        return zepProjectRestClient.getProjectByName(startDate, endDate, name)
                .onFailure().invoke(e -> logger.warn("Error retrieving project", e))
                .map(ZepResponse::data)
                .map(d -> Optional.ofNullable(d.getFirst()))
                .onItem().ifNull().continueWith(Optional::empty)
                .await().indefinitely();
    }

    public Optional<ZepProjectDetail> getProjectById(int projectId) {
        return zepProjectRestClient.getProjectById(projectId)
                .onFailure().invoke(e -> logger.warn("Error retrieving project", e))
                .map(response -> Optional.ofNullable(response.data()))
                .onItem().ifNull().continueWith(Optional::empty)
                .await().indefinitely();
    }


    public List<ZepProjectEmployee> getProjectEmployeesForId(int projectId) {
        return Multi.createBy().repeating()
                .uni(AtomicInteger::new, page ->
                        zepProjectRestClient.getProjectEmployees(projectId, page.incrementAndGet())
                                .onFailure().invoke(ex -> logger.warn("Error retrieving project employees from ZEP", ex))
                )
                .whilst(ZepResponse::hasNext)
                .map(ZepResponse::data)
                .onItem().<ZepProjectEmployee>disjoint()
                .collect().asList()
                .await().indefinitely();
    }
}
