package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gepardec.mega.application.jackson.serializer.RegularWorkingHoursDeserializer;
import com.gepardec.mega.application.jackson.serializer.RegularWorkingHoursSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

@Jacksonized
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeDto {
    @JsonProperty
    private String userId;

    @JsonProperty
    private String email;

    @JsonProperty
    private String title;

    @JsonProperty
    private String firstname;

    @JsonProperty
    private String lastname;

    @JsonProperty
    private String salutation;

    @JsonProperty
    private String releaseDate;

    @JsonProperty
    private String workDescription;

    @JsonProperty
    private String language;

    @JsonProperty
    @JsonSerialize(using = RegularWorkingHoursSerializer.class)
    @JsonDeserialize(using = RegularWorkingHoursDeserializer.class)
    private Map<DayOfWeek, Duration> regularWorkingHours;

    @JsonProperty
    private boolean active;

    /**
     * Austrittsdatum, wird durch Aufruf von employeeService.getAllEmployeesConsideringExitDate befüllt,
     * wenn Mitarbeiter inaktiv ist.
     */
    @JsonProperty
    private LocalDate exitDate;
}
