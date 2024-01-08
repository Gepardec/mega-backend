package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@Jacksonized
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrematureEmployeeCheckDto {

    @JsonProperty
    private UserDto user;

    @JsonProperty
    private LocalDate forMonth;

    @JsonProperty
    private String reason;

    @JsonProperty
    private PrematureEmployeeCheckState state;
}
