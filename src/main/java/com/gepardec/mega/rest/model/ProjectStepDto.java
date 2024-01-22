package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.Employee;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectStepDto {
    @JsonProperty
    private  Long stepId;

    @JsonProperty
    private  Employee employee;

    @JsonProperty
    private  String projectName;

    @JsonProperty
    private  String currentMonthYear;

    @JsonProperty
    private  EmployeeState newState;

    public ProjectStepDto() {
    }

    public ProjectStepDto(Long stepId, Employee employee, String projectName, String currentMonthYear, EmployeeState newState) {
        this.stepId = stepId;
        this.employee = employee;
        this.projectName = projectName;
        this.currentMonthYear = currentMonthYear;
        this.newState = newState;
    }

    public static ProjectStepDtoBuilder builder() {
        return ProjectStepDtoBuilder.aProjectStepDto();
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

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public static final class ProjectStepDtoBuilder {
        private Long stepId;
        private Employee employee;
        private String projectName;
        private String currentMonthYear;
        private EmployeeState newState;

        private ProjectStepDtoBuilder() {
        }

        public static ProjectStepDtoBuilder aProjectStepDto() {
            return new ProjectStepDtoBuilder();
        }

        public ProjectStepDtoBuilder stepId(Long stepId) {
            this.stepId = stepId;
            return this;
        }

        public ProjectStepDtoBuilder employee(Employee employee) {
            this.employee = employee;
            return this;
        }

        public ProjectStepDtoBuilder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public ProjectStepDtoBuilder currentMonthYear(String currentMonthYear) {
            this.currentMonthYear = currentMonthYear;
            return this;
        }

        public ProjectStepDtoBuilder newState(EmployeeState newState) {
            this.newState = newState;
            return this;
        }

        public ProjectStepDto build() {
            ProjectStepDto projectStepDto = new ProjectStepDto();
            projectStepDto.setStepId(stepId);
            projectStepDto.setEmployee(employee);
            projectStepDto.setProjectName(projectName);
            projectStepDto.setCurrentMonthYear(currentMonthYear);
            projectStepDto.setNewState(newState);
            return projectStepDto;
        }
    }
}
