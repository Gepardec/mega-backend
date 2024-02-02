package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.Employee;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateEmployeeStepDto {

    private final Long stepId;

    private final Employee employee;

    private final String currentMonthYear;

    private final EmployeeState newState;

    private final String newStateReason;

    @JsonCreator
    private UpdateEmployeeStepDto(Builder builder) {
        this.stepId = builder.stepId;
        this.employee = builder.employee;
        this.currentMonthYear = builder.currentMonthYear;
        this.newState = builder.newState;
        this.newStateReason = builder.newStateReason;
    }

    public static Builder builder() {
        return Builder.anUpdateEmployeeStepDto();
    }

    public Long getStepId() {
        return stepId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public String getCurrentMonthYear() {
        return currentMonthYear;
    }

    public EmployeeState getNewState() {
        return newState;
    }

    public String getNewStateReason() {
        return newStateReason;
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

    public static final class Builder {
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

        private Builder() {
        }

        public static Builder anUpdateEmployeeStepDto() {
            return new Builder();
        }

        public Builder stepId(Long stepId) {
            this.stepId = stepId;
            return this;
        }

        public Builder employee(Employee employee) {
            this.employee = employee;
            return this;
        }

        public Builder currentMonthYear(String currentMonthYear) {
            this.currentMonthYear = currentMonthYear;
            return this;
        }

        public Builder newState(EmployeeState newState) {
            this.newState = newState;
            return this;
        }

        public Builder newStateReason(String newStateReason) {
            this.newStateReason = newStateReason;
            return this;
        }

        public UpdateEmployeeStepDto build() {
            return new UpdateEmployeeStepDto(this);
        }
    }
}
