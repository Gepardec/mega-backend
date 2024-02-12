package com.gepardec.mega.zep.util;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.ZepServiceTooManyRequestsException;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;


public class Paginator {
    public static <T> List<T> retrieveAll(Function<Integer, Response> pageSupplier, Class<T> elementClass) {
        int page = 1;
        return retrieveAll(pageSupplier, page, elementClass);
    }

    private static <T> List<T> retrieveAll(Function<Integer, Response> pageSupplier, Integer page, Class<T> elementClass) {
        String responseBodyAsString = responseBodyOf(pageSupplier.apply(page));

        Class<T[]> arrayClass = convertClassToArrayClass(elementClass);
        T[] data = arrayClass.cast(ZepRestUtil
                .parseJson(responseBodyAsString, "/data", arrayClass)
                .orElse((T[]) Array.newInstance(elementClass, 0)));

        List<T> result = new ArrayList<>(Arrays.asList(data));

        Optional<String> next = ZepRestUtil.parseJson(responseBodyAsString, "/links/next", String.class);

        if (next.isPresent()) {

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

            return searchInAll(pageSupplier, filter, page + 1, elementClass);
        }
        return Optional.empty();
    }


    private static String responseBodyOf(Response response) {
        try (response) {
            //Header indicating the remaining requests until the rate limit is reached
            String xRateHeader = response.getHeaderString("X-RateLimit-Remaining");
            if (xRateHeader == null) return response.readEntity(String.class);

            //If we reach the rate limit, we throw an exception
            int rate = Integer.parseInt(response.getHeaderString("X-RateLimit-Remaining"));
            if (response.getStatus() == 429 || rate == 0) {
                response.getHeaders().forEach((k, v) -> System.out.println(k + ":" + v));
                throw new ZepServiceTooManyRequestsException("Too many requests to ZEP REST");
            }
            //Math to throttle requests if we approach the rate limit for requests
            if (rate < 10) {
                Long sleepTime = (long) (10 / (Math.exp(rate / 2.5)) * 1000);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            }
            return response.readEntity(String.class);
        } catch (ZepServiceTooManyRequestsException e) {
            throw e;
        } catch (Exception e) {
            throw new ZepServiceException("Error while reading response body", e);
        }
    }

    private static <T> Optional<T> filterList(List<T> list, Predicate<T> filter) {
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
