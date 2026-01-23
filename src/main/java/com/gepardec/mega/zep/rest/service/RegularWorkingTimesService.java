package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class RegularWorkingTimesService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    Logger logger;

    public List<ZepRegularWorkingTimes> getRegularWorkingTimesByUsername(String username) {
        return Multi.createBy().repeating()
                .uni(AtomicInteger::new, page ->
                        zepEmployeeRestClient.getRegularWorkingTimesByUsername(username, page.incrementAndGet())
                                .onFailure().invoke(ex -> logger.warn("Error retrieving regular working times from ZEP", ex))
                )
                .whilst(ZepResponse::hasNext)
                .map(ZepResponse::data)
                .onItem().<ZepRegularWorkingTimes>disjoint()
                .collect().asList()
                .await().indefinitely();
    }
}
