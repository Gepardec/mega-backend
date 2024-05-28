package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepAbsenceRestClient;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.util.ResponseParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AbsenceService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @RestClient
    ZepAbsenceRestClient zepAbsenceRestClient;

    @Inject
    Logger logger;

    @Inject
    ResponseParser responseParser;

    public List<ZepAbsence> getZepAbsencesByEmployeeNameForDateRange(String employeeName, LocalDate start, LocalDate end) {
        try {
            List<ZepAbsence> absences = responseParser.retrieveAll(
                    page -> zepEmployeeRestClient.getAbsencesByUsername(employeeName, page),
                    ZepAbsence.class
            );

            List<ZepAbsence> filteredAbsences = absences.stream()
                                                        .filter(absence -> datesInRange(absence.startDate(), absence.endDate(), start, end))
                                                        .toList();
            return getFullZepAbsences(filteredAbsences);
        }  catch (ZepServiceException e) {
            logger.warn("Error retrieving employee + \"%s\" from ZEP: No /data field in response".formatted(employeeName),
                    e);
            return List.of();
        }
    }

    public ZepAbsence getZepAbsenceById(int id) {
        try {
            return responseParser.retrieveSingle(zepAbsenceRestClient.getAbsenceById(id),
                    ZepAbsence.class).orElse(null);
        } catch (ZepServiceException e) {
            logger.warn("Error retrieving absence + \"%s\" from ZEP: No /data field in response".formatted(id),
                    e);
            return null;
        }
    }

    private List<ZepAbsence> getFullZepAbsences(List<ZepAbsence> zepAbsences) {
        return zepAbsences.stream()
                .map(absence -> getZepAbsenceById(absence.id()))
                .collect(Collectors.toList());
    }

    // this also checks if startDate or endDate is exact match
    private boolean datesInRange(LocalDate startDate, LocalDate endDate, LocalDate fromDateForRequest, LocalDate toDateForRequest) {
        return ((startDate.equals(fromDateForRequest) && endDate.equals(toDateForRequest)) ||
                (startDate.equals(fromDateForRequest) && endDate.isBefore(toDateForRequest)) ||
                (startDate.isAfter(fromDateForRequest) && endDate.equals(toDateForRequest)) ||
                (startDate.isAfter(fromDateForRequest) && endDate.isBefore(toDateForRequest)));
    }
}
