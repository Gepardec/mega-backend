package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ZepResponse<T>(T data, Links links) {

    public boolean hasNext() {
        return links.hasNext();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Links(String prev, String next) {

        public boolean hasNext() {
            return next != null && !next.isBlank();
        }
    }
}
