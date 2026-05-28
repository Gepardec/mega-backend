package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepAbsenceRestClient;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class AbsenceService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @RestClient
    ZepAbsenceRestClient zepAbsenceRestClient;

    @Inject
    Logger logger;

    public List<ZepAbsence> getZepAbsencesByEmployeeNameForDateRange(String employeeName, YearMonth payrollMonth) {
        LocalDate startDate = payrollMonth.atDay(1);
        LocalDate endDate = payrollMonth.atEndOfMonth();

        var filteredAbsences = Multi.createBy().repeating()
                .uni(AtomicInteger::new, page ->
                        zepEmployeeRestClient.getAbsencesByUsername(employeeName, page.incrementAndGet())
                                .onFailure().invoke(ex -> logger.warn("Error retrieving absences from ZEP", ex))
                )
                .whilst(ZepResponse::hasNext)
                .map(ZepResponse::data)
                .onItem().<ZepAbsence>disjoint()
                .collect().asList()
                .await().indefinitely()
                .stream()
                .filter(absence -> datesInRange(absence.startDate(), absence.endDate(), startDate, endDate))
                .toList();

        return getFullZepAbsences(filteredAbsences);
    }

    public Optional<ZepAbsence> getZepAbsenceById(int id) {
        return zepAbsenceRestClient.getAbsenceById(id)
                .onFailure().invoke(e -> logger.warn("Error retrieving absence", e))
                .map(response -> Optional.ofNullable(response.data()))
                .onItem().ifNull().continueWith(Optional::empty)
                .await().indefinitely();
    }

    private List<ZepAbsence> getFullZepAbsences(List<ZepAbsence> zepAbsences) {
        return zepAbsences.stream()
                .map(absence -> getZepAbsenceById(absence.id()))
                .filter(Optional::isPresent)
                .map(Optional::get)
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
