package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class EmployeeService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    Logger logger;

    public Optional<ZepEmployee> getZepEmployeeByUsername(String name) {
        return zepEmployeeRestClient.getByUsername(name)
                .onFailure().invoke(e -> logger.warn("Error retrieving employee", e))
                .map(response -> Optional.ofNullable(response.data()))
                .onItem().ifNull().continueWith(Optional::empty)
                .await().indefinitely();
    }

    public List<ZepEmployee> getZepEmployees() {
        return Multi.createBy().repeating()
                .uni(AtomicInteger::new, page ->
                        zepEmployeeRestClient.getAllEmployeesOfPage(page.incrementAndGet())
                                .onFailure().invoke(ex -> logger.warn("Error retrieving employees from ZEP", ex))
                )
                .whilst(ZepResponse::hasNext)
                .map(ZepResponse::data)
                .onItem().<ZepEmployee>disjoint()
                .collect().asList()
                .await().indefinitely();
    }
}
