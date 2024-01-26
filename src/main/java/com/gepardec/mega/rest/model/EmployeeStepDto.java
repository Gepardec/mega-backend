package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeStepDto {
    private final Long stepId;

    private final EmployeeDto employee;

    private final String currentMonthYear;

    @JsonCreator
    public EmployeeStepDto(Builder builder) {
        this.stepId = builder.stepId;
        this.employee = builder.employee;
        this.currentMonthYear = builder.currentMonthYear;
    }

    public static Builder builder() {
        return Builder.anEmployeeStepDto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeStepDto that = (EmployeeStepDto) o;
        return Objects.equals(getStepId(), that.getStepId()) && Objects.equals(getEmployee(), that.getEmployee()) && Objects.equals(getCurrentMonthYear(), that.getCurrentMonthYear());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStepId(), getEmployee(), getCurrentMonthYear());
    }

    public Long getStepId() {
        return stepId;
    }


    public EmployeeDto getEmployee() {
        return employee;
    }

    public String getCurrentMonthYear() {
        return currentMonthYear;
    }

    public static final class Builder {
        @JsonProperty private Long stepId;
        @JsonProperty private EmployeeDto employee;
        @JsonProperty private String currentMonthYear;

        private Builder() {
        }

        public static Builder anEmployeeStepDto() {
            return new Builder();
        }

        public Builder stepId(Long stepId) {
            this.stepId = stepId;
            return this;
        }

        public Builder employee(EmployeeDto employee) {
            this.employee = employee;
            return this;
        }

        public Builder currentMonthYear(String currentMonthYear) {
            this.currentMonthYear = currentMonthYear;
            return this;
        }

        public EmployeeStepDto build() {
           return new EmployeeStepDto(this);
        }
    }
}
