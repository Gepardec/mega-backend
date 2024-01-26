package com.gepardec.mega.zep.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class Paginator {

    public <T> List<T> retrieveAll(ThreeParamRequestFunction<String, String, String> function, String a, String b, String c, Integer page, Class<T> resultClass) {
        List<T> result = new ArrayList<>();
        try (Response response = function.apply(a, b, c, page);) {
            String responseBodyAsString = response.readEntity(String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            JsonNode dataNode = objectMapper.readTree(responseBodyAsString).get("data");
            if (dataNode.isArray()) {
                for (JsonNode arrayItem : dataNode) {
                    result.add(objectMapper.convertValue(arrayItem, resultClass));
                }
            }
            String next = (String) ZepRestUtil.parseJson(responseBodyAsString, "/links/next", String.class);
            if(next != null) {
                System.out.println("Page: " + page);
                result.addAll(retrieveAll(function, a, b, c, page + 1, resultClass));
            }

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}

