package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@Builder
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
@Accessors(fluent = true)
public class MappedTimeWarning {
    private final LocalDate date;

    private final String description;
}
