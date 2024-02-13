package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepAbsenceRestClient;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepAbsence;
import com.gepardec.mega.zep.util.Paginator;
import com.gepardec.mega.zep.util.ZepRestUtil;
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

    public List<ZepAbsence> getZepAbsencesByEmployeeNameForDateRange(String employeeName, LocalDate start, LocalDate end) {
        try {
            List<ZepAbsence> absences = Paginator.retrieveAll(
                    page -> zepEmployeeRestClient.getAbsencesByUsername(employeeName, start, end, page),
                    ZepAbsence.class
            );
            return getFullZepAbsences(absences);
        }  catch (ZepServiceException e) {
            logger.warn("Error retrieving employee + \"%s\" from ZEP: No /data field in response".formatted(employeeName),
                    e);
            return List.of();
        }
    }

    public ZepAbsence getZepAbsenceById(int id) {
        try {
            return ZepRestUtil.parseJson(zepAbsenceRestClient.getAbsenceById(id).readEntity(String.class),
                    "/data",
                    ZepAbsence.class).orElse(null);
        } catch (ZepServiceException e) {
            logger.warn("Error retrieving absence + \"%s\" from ZEP: No /data field in response".formatted(id),
                    e);
            return null;
        }
    }

    private List<ZepAbsence> getFullZepAbsences(List<ZepAbsence> zepAbsences) {
        return zepAbsences.stream()
                .map(absence -> getZepAbsenceById(absence.getId()))
                .collect(Collectors.toList());
    }
}
