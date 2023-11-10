package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Builder(builderClassName = "Builder")
@Getter
@ToString
@EqualsAndHashCode
@Accessors(fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class NewCommentEntryDto {

    @JsonProperty
    private final Long stepId;

    @JsonProperty
    private final String employeeEmail;

    @JsonProperty
    private final String comment;

    @JsonProperty
    private final String assigneeEmail;

    @JsonProperty
    private final String project;

    @JsonProperty
    private final String currentMonthYear;
}
