package com.gepardec.mega.zep.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.zep.rest.entity.ZepAbsence;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public class ZepRestUtil {
    public static Object parseJson (String json, String path, Class<?> resultClass) {
        return parseJson(json, path, resultClass, false);
    }
    public static Object parseJson (String json, String path, Class<?> resultClass, boolean listConversion) {
        var objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        try {
            JsonNode jsonNode = objectMapper.readTree(json).at(path);
            if (jsonNode.isMissingNode()) {
                throw new RuntimeException("No Node found at " + path);
            }
            Object value = objectMapper.treeToValue(jsonNode, resultClass);

            if(listConversion)
                if (value instanceof Object[])
                    return List.of(value);

            return value;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
