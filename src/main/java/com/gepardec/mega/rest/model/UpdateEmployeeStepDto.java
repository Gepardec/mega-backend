package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.Employee;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateEmployeeStepDto {
    @JsonProperty
    private Long stepId;

    @JsonProperty
    private Employee employee;

    @JsonProperty
    private String currentMonthYear;

    @JsonProperty
    private EmployeeState newState;

    @JsonProperty
    private String newStateReason;

    public UpdateEmployeeStepDto() {
    }

    public UpdateEmployeeStepDto(Long stepId, Employee employee, String currentMonthYear, EmployeeState newState, String newStateReason) {
        this.stepId = stepId;
        this.employee = employee;
        this.currentMonthYear = currentMonthYear;
        this.newState = newState;
        this.newStateReason = newStateReason;
    }

    public static UpdateEmployeeStepDtoBuilder builder() {
        return UpdateEmployeeStepDtoBuilder.anUpdateEmployeeStepDto();
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getCurrentMonthYear() {
        return currentMonthYear;
    }

    public void setCurrentMonthYear(String currentMonthYear) {
        this.currentMonthYear = currentMonthYear;
    }

    public EmployeeState getNewState() {
        return newState;
    }

    public void setNewState(EmployeeState newState) {
        this.newState = newState;
    }

    public String getNewStateReason() {
        return newStateReason;
    }

    public void setNewStateReason(String newStateReason) {
        this.newStateReason = newStateReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateEmployeeStepDto that = (UpdateEmployeeStepDto) o;
        return Objects.equals(getStepId(), that.getStepId()) && Objects.equals(getEmployee(), that.getEmployee()) && Objects.equals(getCurrentMonthYear(), that.getCurrentMonthYear()) && getNewState() == that.getNewState() && Objects.equals(getNewStateReason(), that.getNewStateReason());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStepId(), getEmployee(), getCurrentMonthYear(), getNewState(), getNewStateReason());
    }

    public static final class UpdateEmployeeStepDtoBuilder {
        private Long stepId;
        private Employee employee;
        private String currentMonthYear;
        private EmployeeState newState;
        private String newStateReason;

        private UpdateEmployeeStepDtoBuilder() {
        }

        public static UpdateEmployeeStepDtoBuilder anUpdateEmployeeStepDto() {
            return new UpdateEmployeeStepDtoBuilder();
        }

        public UpdateEmployeeStepDtoBuilder stepId(Long stepId) {
            this.stepId = stepId;
            return this;
        }

        public UpdateEmployeeStepDtoBuilder employee(Employee employee) {
            this.employee = employee;
            return this;
        }

        public UpdateEmployeeStepDtoBuilder currentMonthYear(String currentMonthYear) {
            this.currentMonthYear = currentMonthYear;
            return this;
        }

        public UpdateEmployeeStepDtoBuilder newState(EmployeeState newState) {
            this.newState = newState;
            return this;
        }

        public UpdateEmployeeStepDtoBuilder newStateReason(String newStateReason) {
            this.newStateReason = newStateReason;
            return this;
        }

        public UpdateEmployeeStepDto build() {
            UpdateEmployeeStepDto updateEmployeeStepDto = new UpdateEmployeeStepDto();
            updateEmployeeStepDto.setStepId(stepId);
            updateEmployeeStepDto.setEmployee(employee);
            updateEmployeeStepDto.setCurrentMonthYear(currentMonthYear);
            updateEmployeeStepDto.setNewState(newState);
            updateEmployeeStepDto.setNewStateReason(newStateReason);
            return updateEmployeeStepDto;
        }
    }
}
