package com.gepardec.mega.zep.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.zep.rest.entity.ZepAbsence;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.ParameterizedType;
import java.util.List;
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

            return Optional.ofNullable(objectMapper.treeToValue(jsonNode, resultClass));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
