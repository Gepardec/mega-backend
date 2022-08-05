package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@Builder
@Setter
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
@Accessors(fluent = true)
public class MappedTimeWarningDTO {
    private final LocalDate date;

    public String description;
}
