package com.gepardec.mega.zep.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.zep.ZepServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Paginator {
    public static <T> List<T> retrieveAll(Function<Integer, Response> pageSupplier, Class<T> elementClass) {
        int page = 1;
        return retrieveAll(pageSupplier, page, elementClass);
    }

    private static <T> List<T> retrieveAll(Function<Integer, Response> pageSupplier, Integer page, Class<T> elementClass) {
        String responseBodyAsString = responseBodyOf(pageSupplier.apply(page));
        System.out.println(responseBodyAsString);

        Class<T[]> arrayClass = convertClassToArrayClass(elementClass);
        T[] data = arrayClass.cast(ZepRestUtil
                .parseJson(responseBodyAsString, "/data", arrayClass)
                .orElse((T[]) Array.newInstance(elementClass, 0)));

        List<T> result = new ArrayList<>(Arrays.asList(data));

        Optional<String> next = ZepRestUtil.parseJson(responseBodyAsString, "/links/next", String.class);

        if (next.isPresent()) {
//            System.out.println("Page: " + page);
            result.addAll(retrieveAll(pageSupplier, page + 1, elementClass));
        }
        return result;
    }

    public static <T> Optional<T> searchInAll(Function<Integer, Response> function,
                                              Predicate<T> filter,
                                              Class<T> elementClass) {
        int page = 1;
        return searchInAll(function, filter, page, elementClass);
    }

    private static <T> Optional<T> searchInAll(Function<Integer, Response> pageSupplier,
                                          Predicate<T> filter,
                                          Integer page,
                                          Class<T> elementClass) {

        String responseBodyAsString = responseBodyOf(pageSupplier.apply(page));

        Class<T[]> arrayClass = convertClassToArrayClass(elementClass);
        T[] data = arrayClass.cast(ZepRestUtil.parseJson(responseBodyAsString, "/data", arrayClass)
                .orElse((T[]) Array.newInstance(elementClass, 0)));

        Optional<T> current = filterList(Arrays.asList(data), filter);
        if (current.isPresent()) {
            return current;
        }

        Optional<String> next = ZepRestUtil.parseJson(responseBodyAsString, "/links/next", String.class);

        if (next.isPresent()) {
//            System.out.println("Page: " + page);
            return searchInAll(pageSupplier, filter, page + 1, elementClass);
        }
        return Optional.empty();
    }



     private static String responseBodyOf(Response response) {
        try (response) {
            return response.readEntity(String.class);
        }
        catch (Exception e) {
            throw new ZepServiceException("Error while reading response body",e);
        }
    }

    private static <T> Optional<T> filterList(List<T> list , Predicate<T> filter) {
        return list.stream()
                .filter(filter)
                .findFirst();
    }

    private static <T> Class<T[]> convertClassToArrayClass(Class<T> targetClass) {
        return (Class<T[]>) Array.newInstance(targetClass, 0).getClass();
    }
    private static <T> Class<T[]> getEmptyArrayOf(Class<T> targetClass) {
        return (Class<T[]>) Array.newInstance(targetClass, 0).getClass();
    }


}
