package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepAbsenceRestClient;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepAbsence;
import com.gepardec.mega.zep.util.Paginator;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class AbsenceService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @RestClient
    ZepAbsenceRestClient zepAbsenceRestClient;

    public ZepAbsence[] getZepAbsencesByEmployeeName(String employeeName) {
        return Paginator.retrieveAll(
                page -> zepEmployeeRestClient.getAbsencesByUsername(employeeName, page),
                ZepAbsence.class
        ).toArray(ZepAbsence[]::new);
    }
//
//    public void setZepAbsenceReason(ZepAbsence zepAbsence) {
//        try (Response resp = zepAbsenceRestClient.getAbsenceById(zepAbsence.getId())) {
//            String output = resp.readEntity(String.class);
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.findAndRegisterModules();
//            JsonNode jsonNode = objectMapper.readTree(output);
//            String absenceText = (objectMapper.treeToValue(jsonNode.at("/data/absence_text"), String.class));
//            zepAbsence.setAbsenceReason(absenceText);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
}
