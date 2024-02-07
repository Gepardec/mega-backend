package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import jakarta.annotation.Nullable;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PmProgressDto {
    private final String project;

    private final String assigneeEmail;

    private final String firstname;

    private final String lastname;

    private final EmployeeState state;

    private final Long stepId;


    @JsonCreator
    private PmProgressDto(Builder builder) {
        this.project = builder.project;
        this.assigneeEmail = builder.assigneeEmail;
        this.firstname = builder.firstname;
        this.lastname = builder.lastname;
        this.state = builder.state;
        this.stepId = builder.stepId;
    }

    public static Builder builder() {
        return Builder.aPmProgressDto();
    }

    @Nullable
    public String getProject() {
        return project;
    }

    public String getAssigneeEmail() {
        return assigneeEmail;
    }

    @Nullable
    public String getFirstname() {
        return firstname;
    }

    @Nullable
    public String getLastname() {
        return lastname;
    }

    public EmployeeState getState() {
        return state;
    }

    public Long getStepId() {
        return stepId;
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

    public static final class Builder {
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

        private Builder() {
        }

        public static Builder aPmProgressDto() {
            return new Builder();
        }

        public Builder project(String project) {
            this.project = project;
            return this;
        }

        public Builder assigneeEmail(String assigneeEmail) {
            this.assigneeEmail = assigneeEmail;
            return this;
        }

        public Builder firstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public Builder lastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public Builder state(EmployeeState state) {
            this.state = state;
            return this;
        }

        public Builder stepId(Long stepId) {
            this.stepId = stepId;
            return this;
        }

        public PmProgressDto build() {
            return new PmProgressDto(this);
        }
    }
}
