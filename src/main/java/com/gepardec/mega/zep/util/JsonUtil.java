package com.gepardec.mega.zep.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.gepardec.mega.zep.ZepServiceException;
import java.util.Optional;


public class JsonUtil {

    public static <T> Optional<T> parseJson (String json, String path, Class<T> resultClass) {
        var objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        try {
            JsonNode jsonNode = objectMapper.readTree(json).at(path);
            if (jsonNode.isMissingNode()) {
                throw new ZepServiceException("Missing JSON-Path: " + path);
            }

            if (jsonNode.isNull()) {
                return Optional.empty();
            }

            if ((jsonNode.isArray() || jsonNode.isObject()) && jsonNode.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(objectMapper.treeToValue(jsonNode, resultClass));
        }  catch (JsonProcessingException e) {
            if (e instanceof MismatchedInputException mme) {
                throw new RuntimeException(mme.getMessage());
            }
            throw new ZepServiceException("Error while parsing json to " + resultClass.getName(), e);
        }
    }
}
