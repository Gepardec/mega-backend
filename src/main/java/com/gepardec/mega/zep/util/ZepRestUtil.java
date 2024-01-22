package com.gepardec.mega.zep.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.zep.rest.entity.ZepAbsence;
import jakarta.ws.rs.core.Response;

public class ZepRestUtil {
    public static Object responseToType (Response resp, Class<?> resultClass) throws JsonProcessingException {
        String output = resp.readEntity(String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        JsonNode jsonNode = objectMapper.readTree(output);
        return objectMapper.treeToValue(jsonNode.get("data"), resultClass);
    }
}
