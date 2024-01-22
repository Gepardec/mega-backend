package com.gepardec.mega.zep.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.zep.rest.client.ZepAbsenceRestClient;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepAbsence;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import com.gepardec.mega.zep.util.ZepRestUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@ApplicationScoped
public class AbsenceService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @RestClient
    ZepAbsenceRestClient zepAbsenceRestClient;

    //TODO: Pagination handling
    public ZepAbsence[] getZepAbsencesByEmployeeName(String employeeName) {
        try (Response resp = zepEmployeeRestClient.getAbsencesByUsername(employeeName)) {
            String output = resp.readEntity(String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            JsonNode jsonNode = objectMapper.readTree(output);
            ZepAbsence[] absences = objectMapper.treeToValue(jsonNode.get("data"), ZepAbsence[].class);
            Arrays.stream(absences)
                    .forEach(this::setZepAbsenceReason);
            return absences;

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setZepAbsenceReason(ZepAbsence zepAbsence) {
        try (Response resp = zepAbsenceRestClient.getAbsenceById(zepAbsence.getId())) {
            String output = resp.readEntity(String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            JsonNode jsonNode = objectMapper.readTree(output);
            String absenceText = (objectMapper.treeToValue(jsonNode.at("/data/absence_text"), String.class));
            zepAbsence.setAbsenceReason(absenceText);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
