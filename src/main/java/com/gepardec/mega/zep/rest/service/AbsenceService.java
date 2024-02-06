package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepAbsenceRestClient;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepAbsence;
import com.gepardec.mega.zep.util.Paginator;
import com.gepardec.mega.zep.util.ZepRestUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AbsenceService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @RestClient
    ZepAbsenceRestClient zepAbsenceRestClient;

    public List<ZepAbsence> getZepAbsencesByEmployeeNameForDateRange(String employeeName, LocalDate start, LocalDate end) {
        List<ZepAbsence> absences = Paginator.retrieveAll(
                page -> zepEmployeeRestClient.getAbsencesByUsername(employeeName, start, end, page),
                ZepAbsence.class
        );

        return getFullZepAbsences(absences);
    }

    public ZepAbsence getZepAbsenceById(int id) {
        return ZepRestUtil.parseJson(zepAbsenceRestClient.getAbsenceById(id).readEntity(String.class),
                "/data",
                ZepAbsence.class).orElse(null);
    }

    private List<ZepAbsence> getFullZepAbsences(List<ZepAbsence> zepAbsences) {
        return zepAbsences.stream()
                .map(absence -> getZepAbsenceById(absence.getId()))
                .collect(Collectors.toList());
    }
}
