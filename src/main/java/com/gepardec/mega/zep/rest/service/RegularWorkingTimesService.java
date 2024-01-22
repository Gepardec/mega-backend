package com.gepardec.mega.zep.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepRegularWorkingTimes;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class RegularWorkingTimesService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    public ZepRegularWorkingTimes getRegularWorkingTimesByUsername(String username) {
        try (Response resp = zepEmployeeRestClient.getRegularWorkingTimesByUsername(username)) {

            if (resp.getStatus() == 404) {
                throw new ZepServiceException("Error from ZEP endpoint /" + username + "/regular-working-times: 404 Not Found");
            }
            if (resp.getStatus() == 401) {
                throw new ZepServiceException("Error from ZEP endpoint /" + username + "/regular-working-times: 401 Not Authorized");
            }

            String responseBodyAsString = resp.readEntity(String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBodyAsString);

            ZepRegularWorkingTimes workingTimes[] = objectMapper.treeToValue(jsonNode.get("data"), ZepRegularWorkingTimes[].class);

            if (workingTimes.length == 0) {
                throw new ZepServiceException("Data empty");
            }
            return workingTimes[0];
        } catch (JsonProcessingException e) {
            throw new ZepServiceException("Error parsing the return data of /" + username + "/regular-working-times", e);
        }
    }
}
