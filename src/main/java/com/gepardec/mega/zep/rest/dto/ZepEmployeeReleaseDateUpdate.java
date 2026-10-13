package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record ZepEmployeeReleaseDateUpdate(
        String username,
        @JsonProperty("approval_date") LocalDate releaseDate
) {
}
