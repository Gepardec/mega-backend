package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import jakarta.annotation.Nullable;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PmProgressDto {
    @JsonProperty
    @Nullable
    private String project;

    @JsonProperty
    private String assigneeEmail;

    @JsonProperty
    @Nullable
    private String firstname;

    @JsonProperty
    @Nullable
    private String lastname;

    @JsonProperty
    private EmployeeState state;

    @JsonProperty
    private Long stepId;

    public PmProgressDto() {
    }

    public PmProgressDto(@Nullable String project, String assigneeEmail, @Nullable String firstname, @Nullable String lastname, EmployeeState state, Long stepId) {
        this.project = project;
        this.assigneeEmail = assigneeEmail;
        this.firstname = firstname;
        this.lastname = lastname;
        this.state = state;
        this.stepId = stepId;
    }

    public static PmProgressDtoBuilder builder() {
        return PmProgressDtoBuilder.aPmProgressDto();
    }

    @Nullable
    public String getProject() {
        return project;
    }

    public void setProject(@Nullable String project) {
        this.project = project;
    }

    public String getAssigneeEmail() {
        return assigneeEmail;
    }

    public void setAssigneeEmail(String assigneeEmail) {
        this.assigneeEmail = assigneeEmail;
    }

    @Nullable
    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(@Nullable String firstname) {
        this.firstname = firstname;
    }

    @Nullable
    public String getLastname() {
        return lastname;
    }

    public void setLastname(@Nullable String lastname) {
        this.lastname = lastname;
    }

    public EmployeeState getState() {
        return state;
    }

    public void setState(EmployeeState state) {
        this.state = state;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PmProgressDto that = (PmProgressDto) o;
        return Objects.equals(getProject(), that.getProject()) && Objects.equals(getAssigneeEmail(), that.getAssigneeEmail()) && Objects.equals(getFirstname(), that.getFirstname()) && Objects.equals(getLastname(), that.getLastname()) && getState() == that.getState() && Objects.equals(getStepId(), that.getStepId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProject(), getAssigneeEmail(), getFirstname(), getLastname(), getState(), getStepId());
    }

    public static PmProgressDto ofStepEntry(StepEntry stepEntry) {
        return PmProgressDto.builder()
                .project(stepEntry.getProject())
                .assigneeEmail(stepEntry.getAssignee().getEmail())
                .firstname(stepEntry.getAssignee().getFirstname())
                .lastname(stepEntry.getAssignee().getLastname())
                .state(stepEntry.getState())
                .stepId(stepEntry.getStep().getId())
                .build();
    }

    public static final class PmProgressDtoBuilder {
        private String project;
        private String assigneeEmail;
        private String firstname;
        private String lastname;
        private EmployeeState state;
        private Long stepId;

        private PmProgressDtoBuilder() {
        }

        public static PmProgressDtoBuilder aPmProgressDto() {
            return new PmProgressDtoBuilder();
        }

        public PmProgressDtoBuilder project(String project) {
            this.project = project;
            return this;
        }

        public PmProgressDtoBuilder assigneeEmail(String assigneeEmail) {
            this.assigneeEmail = assigneeEmail;
            return this;
        }

        public PmProgressDtoBuilder firstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public PmProgressDtoBuilder lastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public PmProgressDtoBuilder state(EmployeeState state) {
            this.state = state;
            return this;
        }

        public PmProgressDtoBuilder stepId(Long stepId) {
            this.stepId = stepId;
            return this;
        }

        public PmProgressDto build() {
            PmProgressDto pmProgressDto = new PmProgressDto();
            pmProgressDto.setProject(project);
            pmProgressDto.setAssigneeEmail(assigneeEmail);
            pmProgressDto.setFirstname(firstname);
            pmProgressDto.setLastname(lastname);
            pmProgressDto.setState(state);
            pmProgressDto.setStepId(stepId);
            return pmProgressDto;
        }
    }
}
