package com.gepardec.mega.zep.util;

import com.gepardec.mega.zep.rest.dto.ZepApiResponse;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.function.Function;

@ApplicationScoped
public class PaginationHelper {

    public <T> Multi<T> fetchAllPages(Function<Integer, Uni<ZepApiResponse<T>>> fetchFn) {
        return fetchPage(fetchFn, 1);
    }

    private <T> Multi<T> fetchPage(Function<Integer, Uni<ZepApiResponse<T>>> fetchFn, int page) {
        return Multi.createFrom().uni(fetchFn.apply(page))
                .onItem().transformToMultiAndConcatenate(response -> {
                    Multi<T> currentItems = Multi.createFrom().iterable(response.data());

                    if (response.hasNextPage()) {
                        return Multi.createBy().concatenating().streams(
                                currentItems,
                                fetchPage(fetchFn, response.nextPageNumber()
                                ));
                    } else {
                        return currentItems;
                    }
                });
    }
}
