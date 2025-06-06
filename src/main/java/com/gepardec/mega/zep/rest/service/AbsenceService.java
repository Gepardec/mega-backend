package com.gepardec.mega.zep.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepAbsenceRestClient;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
public class AbsenceService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @RestClient
    ZepAbsenceRestClient zepAbsenceRestClient;

    @Inject
    Logger logger;

    @RateLimit(value = 6, window = 1, windowUnit = ChronoUnit.SECONDS)
    public List<ZepAbsence> getZepAbsencesByEmployeeNameForDateRange(String employeeName, YearMonth payrollMonth) {
        LocalDate startDate = payrollMonth.atDay(1);
        LocalDate endDate = payrollMonth.atEndOfMonth();

        try {

            Multi<ZepAbsence> absences = Multi.createBy().repeating().uni(AtomicInteger::new, page ->
                            zepEmployeeRestClient.getAbsencesByUsername(employeeName, page.incrementAndGet())
                    )
                    .until(List::isEmpty)
                    .onItem().disjoint();

            Multi<ZepAbsence> filteredAbsences = absences.filter(absence ->
                    datesInRange(absence.startDate(), absence.endDate(), startDate, endDate)
            );

            List<ZepAbsence> filteredAbsenceList = filteredAbsences.collect().asList()
                    .await().indefinitely();

            return getFullZepAbsences(filteredAbsenceList);
        } catch (ZepServiceException e) {
            String message = "Error retrieving employee \"%s\" from ZEP: No /data field in response".formatted(employeeName);
            logger.warn(message, e);
            return List.of();
        }
    }

    public ZepAbsence getZepAbsenceById(int id) {
        try {

            Multi<ZepAbsence> absences = Multi.createBy()
                    .repeating().uni(AtomicInteger::new, idCounter ->
                            zepAbsenceRestClient.getAbsenceById(id)
                    )
                    .until(absence -> absence == null);

            return absences.collect().first().await().indefinitely();
        } catch (ZepServiceException e) {
            String message = "Error retrieving absence \"%s\" from ZEP: No /data field in response".formatted(id);
            logger.warn(message, e);
            return null;
        }
    }

    private List<ZepAbsence> getFullZepAbsences(List<ZepAbsence> zepAbsences) {
        return zepAbsences.stream()
                .map(absence -> getZepAbsenceById(absence.id()))
                .toList();
    }

    // this also checks if startDate or endDate is exact match
    private boolean datesInRange(LocalDate startDate, LocalDate endDate, LocalDate fromDateForRequest, LocalDate toDateForRequest) {
        return ((startDate.equals(fromDateForRequest) && endDate.equals(toDateForRequest)) ||
                (startDate.equals(fromDateForRequest) && endDate.isBefore(toDateForRequest)) ||
                (startDate.isAfter(fromDateForRequest) && endDate.equals(toDateForRequest)) ||
                (startDate.isAfter(fromDateForRequest) && endDate.isBefore(toDateForRequest)));
    }
}
