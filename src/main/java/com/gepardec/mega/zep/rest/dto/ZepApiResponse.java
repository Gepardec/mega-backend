package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ZepApiResponse<T>(
        @JsonProperty("data")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<T> data,

        @JsonProperty("links")
        PaginationLinks links,

        @JsonProperty("meta")
        PaginationMeta meta
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaginationLinks(String next) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaginationMeta(
            @JsonProperty("current_page") int currentPage
    ) { }

    public boolean hasNextPage() {
        return links != null && links.next() != null;
    }

    public int nextPageNumber() {
        return meta.currentPage() + 1;
    }
}
