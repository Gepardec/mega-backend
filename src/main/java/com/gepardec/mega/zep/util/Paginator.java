package com.gepardec.mega.zep.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.zep.ZepServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Paginator {

    public static <T> List<T> retrieveAll(Function<Integer, Response> function, Integer page, Class<T> resultClass) {
        String responseBodyAsString = responseBodyOf(function.apply(page));

        Class<T[]> arrayClass = (Class<T[]>) Array.newInstance(resultClass, 0).getClass();
        T[] data = arrayClass.cast(ZepRestUtil.parseJson(responseBodyAsString, "/data", arrayClass));

        List<T> result = new ArrayList<>(Arrays.asList(data));

        String next = (String) ZepRestUtil.parseJson(responseBodyAsString, "/links/next", String.class);

        if (next != null) {
            System.out.println("Page: " + page);
            result.addAll(retrieveAll(function, page + 1, resultClass));
        }
        return result;
    }

    public static <T> List<T> retrieveAll(Function<Integer, Response> function, Class<T> resultClass) {
        int page = 1;
        return retrieveAll(function, page, resultClass);
    }

    public static String responseBodyOf(Response response) {
        try (response) {
            return response.readEntity(String.class);
        }
    }


}
