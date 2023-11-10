package com.gepardec.mega.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    private String userId;

    private String email;

    private String title;

    private String firstname;

    private String lastname;

    private String salutation;

    private String releaseDate;

    private String workDescription;

    private String language;

    private Map<DayOfWeek, Duration> regularWorkingHours;

    private boolean active;

    /**
     * Austrittsdatum, wird durch Aufruf von employeeService.getAllEmployeesConsideringExitDate befüllt,
     * wenn Mitarbeiter inaktiv ist.
     */
    private LocalDate exitDate;
}
