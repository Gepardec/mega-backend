package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.Duration;
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
        try {
            return Optional.ofNullable(zepEmployeeRestClient.getByUsername(name).await().atMost(Duration.ofSeconds(30)));
        } catch (ZepServiceException e) {
            String message = "Error retrieving employee \"%s\" from ZEP: No /data field in response".formatted(name);
            logger.warn(message, e);
        }
        return Optional.empty();
    }

    public Optional<ZepEmployee> getZepEmployeeByPersonalNumber(String personalNumber) {
        try {
            return Optional.ofNullable(zepEmployeeRestClient.getByPersonalNumber(personalNumber).await().atMost(Duration.ofSeconds(30)));

        } catch (ZepServiceException e) {
            String message = "Error retrieving employee \"%s\" from ZEP: No /data field in response".formatted(personalNumber);
            logger.warn(message, e);
        }

        return Optional.empty();

    }

    public List<ZepEmployee> getZepEmployees() {
        Multi<ZepEmployee> employees = Multi.createBy().repeating().uni(AtomicInteger::new, page ->
                        zepEmployeeRestClient.getAllEmployeesOfPage(page.incrementAndGet())
                )
                .until(List::isEmpty)
                .onItem().disjoint();

        System.out.println("Employees: " + employees);


        return employees.collect().asList()
                .await().indefinitely();
    }

}
