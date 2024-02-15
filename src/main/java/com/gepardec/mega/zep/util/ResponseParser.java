package com.gepardec.mega.zep.util;

import com.gepardec.mega.zep.ZepServiceTooManyRequestsException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ApplicationScoped
public class ResponseParser {

    @Inject
    RequestThrottler requestThrottler;

    public <T> Optional<T> retrieveSingle(Response response, Class<T> elementClass) {
        String responseBodyAsString = readResponse(response);
        return JsonUtil.parseJson(responseBodyAsString, "/data", elementClass);
    }

    public <T> List<T> retrieveAll(Function<Integer, Response> pageSupplier, Class<T> elementClass) {
        int page = 1;
        return retrieveAll(pageSupplier, page, elementClass);
    }

    private <T> List<T> retrieveAll(Function<Integer, Response> pageSupplier, Integer page, Class<T> elementClass) {
        String responseBodyAsString = readResponse(pageSupplier.apply(page));

        Class<T[]> arrayClass = convertClassToArrayClass(elementClass);
        T[] data = arrayClass.cast(JsonUtil
                .parseJson(responseBodyAsString, "/data", arrayClass)
                .orElse((T[]) Array.newInstance(elementClass, 0)));

        List<T> result = new ArrayList<>(Arrays.asList(data));

        Optional<String> next = JsonUtil.parseJson(responseBodyAsString, "/links/next", String.class);

        if (next.isPresent()) {

            result.addAll(retrieveAll(pageSupplier, page + 1, elementClass));
        }
        return result;
    }

    public <T> Optional<T> searchInAll(Function<Integer, Response> function,
                                              Predicate<T> filter,
                                              Class<T> elementClass) {
        int page = 1;
        return this.searchInAll(function, filter, page, elementClass);
    }

    private  <T> Optional<T> searchInAll(Function<Integer, Response> pageSupplier,
                                               Predicate<T> filter,
                                               Integer page,
                                               Class<T> elementClass) {

        String responseBodyAsString = this.readResponse(pageSupplier.apply(page));


        Class<T[]> arrayClass = convertClassToArrayClass(elementClass);
        T[] data = arrayClass.cast(JsonUtil.parseJson(responseBodyAsString, "/data", arrayClass)
                .orElse((T[]) Array.newInstance(elementClass, 0)));

        Optional<T> current = filterList(Arrays.asList(data), filter);
        if (current.isPresent()) {
            return current;
        }

        Optional<String> next = JsonUtil.parseJson(responseBodyAsString, "/links/next", String.class);

        if (next.isPresent()) {

            return searchInAll(pageSupplier, filter, page + 1, elementClass);
        }
        return Optional.empty();
    }


    private String readResponse(Response response) {
        try (response) {
            //If we reach the rate limit, we throw an exception
            if (response.getStatus() == 429 ) {
                throw new ZepServiceTooManyRequestsException("Too many requests to ZEP REST");
            }

            String xRateHeader = readXRateHeader(response);
            if (xRateHeader != null) {
                requestThrottler.setRate(Integer.parseInt(readXRateHeader(response)));
                requestThrottler.throttle();
            }
            return response.readEntity(String.class);
        }
    }

    private static String readXRateHeader(Response response) {
        return response.getHeaderString("X-RateLimit-Remaining");
    }

    private static <T> Optional<T> filterList(List<T> list, Predicate<T> filter) {
        return list.stream()
                .filter(filter)
                .findFirst();
    }

    private static <T> Class<T[]> convertClassToArrayClass(Class<T> targetClass) {
        return (Class<T[]>) Array.newInstance(targetClass, 0).getClass();
    }

}
