package com.gepardec.mega.zep.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.zep.ZepServiceException;

import java.util.Optional;

public class ZepRestUtil {

    public static <T> Optional<T> parseJson (String json, String path, Class<T> resultClass) {
        var objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        try {
            JsonNode jsonNode = objectMapper.readTree(json).at(path);
            if (jsonNode.isMissingNode()) {
                throw new RuntimeException("No Node found at JSON-Path: " + path);
            }

            if (jsonNode.isNull()) {
                return Optional.empty();
            }

            if ((jsonNode.isArray() || jsonNode.isObject()) && jsonNode.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(objectMapper.treeToValue(jsonNode, resultClass));
        } catch (JsonProcessingException e) {
            throw new ZepServiceException("Error while parsing json to " + resultClass.getName(), e);
        }
    }


}
