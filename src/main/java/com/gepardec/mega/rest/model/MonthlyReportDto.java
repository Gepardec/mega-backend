package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarning;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.util.List;

@Jacksonized
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MonthlyReportDto {
    @JsonProperty
    private EmployeeDto employee;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate initialDate;

    @JsonProperty
    private List<MappedTimeWarningDTO> timeWarnings;

    @JsonProperty
    private List<JourneyWarning> journeyWarnings;

    @JsonProperty
    private List<Comment> comments;

    @JsonProperty
    private EmployeeState employeeCheckState;

    @JsonProperty
    private String employeeCheckStateReason;

    @JsonProperty
    private EmployeeState internalCheckState;

    @JsonProperty
    private boolean isAssigned;

    @JsonProperty
    private List<PmProgressDto> employeeProgresses;

    @JsonProperty
    private boolean otherChecksDone;

    @JsonProperty
    private int vacationDays;

    @JsonProperty
    private int homeofficeDays;

    @JsonProperty
    private int compensatoryDays;

    @JsonProperty
    private int nursingDays;

    @JsonProperty
    private int maternityLeaveDays;

    @JsonProperty
    private int externalTrainingDays;

    @JsonProperty
    private int conferenceDays;

    @JsonProperty
    private int maternityProtectionDays;

    @JsonProperty
    private int fatherMonthDays;

    @JsonProperty
    private int paidSpecialLeaveDays;

    @JsonProperty
    private int nonPaidVacationDays;

    @JsonProperty
    private double vacationDayBalance;

    @JsonProperty
    private String billableTime;

    @JsonProperty
    private String totalWorkingTime;

    @JsonProperty
    private int paidSickLeave;

    @JsonProperty
    private double overtime;
}
