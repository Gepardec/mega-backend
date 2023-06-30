package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.State;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder(builderClassName = "Builder")
@Getter
@ToString
@EqualsAndHashCode
@Accessors(fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class ManagementEntryDto {

    @JsonProperty
    private final Employee employee;

    @JsonProperty
    private final State employeeCheckState;

    @JsonProperty
    private final String employeeCheckStateReason;

    @JsonProperty
    private final State internalCheckState;

    @JsonProperty
    private final State projectCheckState;

    @JsonProperty
    @Nullable
    private final List<PmProgressDto> employeeProgresses;

    @JsonProperty
    private final long totalComments;

    @JsonProperty
    private final long finishedComments;

    @JsonProperty
    private final String entryDate;

    @JsonProperty
    private final String billableTime;

    @JsonProperty
    private final String nonBillableTime;
}
