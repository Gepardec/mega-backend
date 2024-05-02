package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.gepardec.mega.db.entity.employee.EmployeeState;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = ProjectStepDto.Builder.class)
public class ProjectStepDto {

    private final Long stepId;

    private final EmployeeDto employee;

    private final String projectName;

    private final String currentMonthYear;

    private EmployeeState newState;

    private ProjectStepDto(Builder builder) {
        this.stepId = builder.stepId;
        this.employee = builder.employee;
        this.projectName = builder.projectName;
        this.currentMonthYear = builder.currentMonthYear;
        this.newState = builder.newState;
    }

    public static Builder builder() {
        return Builder.aProjectStepDto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectStepDto that = (ProjectStepDto) o;
        return Objects.equals(getStepId(), that.getStepId()) && Objects.equals(getEmployee(), that.getEmployee()) && Objects.equals(getProjectName(), that.getProjectName()) && Objects.equals(getCurrentMonthYear(), that.getCurrentMonthYear()) && getNewState() == that.getNewState();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStepId(), getEmployee(), getProjectName(), getCurrentMonthYear(), getNewState());
    }

    public Long getStepId() {
        return stepId;
    }

    public EmployeeDto getEmployee() {
        return employee;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getCurrentMonthYear() {
        return currentMonthYear;
    }

    public EmployeeState getNewState() {
        return newState;
    }

    public void setNewState(EmployeeState newState) {
        this.newState = newState;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private Long stepId;
        private EmployeeDto employee;
        private String projectName;
        private String currentMonthYear;
        private EmployeeState newState;

        private Builder() {
        }

        public static Builder aProjectStepDto() {
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

        public Builder projectName(String projectName) {
            this.projectName = projectName;
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

        public ProjectStepDto build() {
            return new ProjectStepDto(this);
        }
    }
}
