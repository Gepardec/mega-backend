package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ZepProjectDetail(
        @JsonUnwrapped
        ZepProject project,

        @JsonProperty("categories")
        List<ZepCategory> categories
) {
}
