package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeStepDto {
    @JsonProperty
    private Long stepId;

    @JsonProperty
    private EmployeeDto employee;

    @JsonProperty
    private String currentMonthYear;

    public EmployeeStepDto() {
    }

    public EmployeeStepDto(Long stepId, EmployeeDto employee, String currentMonthYear) {
        this.stepId = stepId;
        this.employee = employee;
        this.currentMonthYear = currentMonthYear;
    }

    public static EmployeeStepDtoBuilder builder() {
        return EmployeeStepDtoBuilder.anEmployeeStepDto();
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

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public String getCurrentMonthYear() {
        return currentMonthYear;
    }

    public void setCurrentMonthYear(String currentMonthYear) {
        this.currentMonthYear = currentMonthYear;
    }

    public static final class EmployeeStepDtoBuilder {
        private Long stepId;
        private EmployeeDto employee;
        private String currentMonthYear;

        private EmployeeStepDtoBuilder() {
        }

        public static EmployeeStepDtoBuilder anEmployeeStepDto() {
            return new EmployeeStepDtoBuilder();
        }

        public EmployeeStepDtoBuilder stepId(Long stepId) {
            this.stepId = stepId;
            return this;
        }

        public EmployeeStepDtoBuilder employee(EmployeeDto employee) {
            this.employee = employee;
            return this;
        }

        public EmployeeStepDtoBuilder currentMonthYear(String currentMonthYear) {
            this.currentMonthYear = currentMonthYear;
            return this;
        }

        public EmployeeStepDto build() {
            EmployeeStepDto employeeStepDto = new EmployeeStepDto();
            employeeStepDto.setStepId(stepId);
            employeeStepDto.setEmployee(employee);
            employeeStepDto.setCurrentMonthYear(currentMonthYear);
            return employeeStepDto;
        }
    }
}
