package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.gepardec.mega.db.entity.employee.EmployeeState;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = UpdateEmployeeStepDto.Builder.class)
public class UpdateEmployeeStepDto {

    private final Long stepId;

    private final EmployeeDto employee;

    private final String currentMonthYear;

    private final EmployeeState newState;

    private final String newStateReason;

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

    public EmployeeDto getEmployee() {
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

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private Long stepId;
        private EmployeeDto employee;
        private String currentMonthYear;
        private EmployeeState newState;
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

        public Builder employee(EmployeeDto employee) {
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
