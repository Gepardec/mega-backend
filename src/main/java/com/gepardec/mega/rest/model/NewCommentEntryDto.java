package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = NewCommentEntryDto.Builder.class)
public class NewCommentEntryDto {

    private final Long stepId;

    private final String employeeEmail;

    private final String comment;

    private final String assigneeEmail;

    private final String project;

    private final String currentMonthYear;

    private NewCommentEntryDto(Builder builder) {
        this.stepId = builder.stepId;
        this.employeeEmail = builder.employeeEmail;
        this.comment = builder.comment;
        this.assigneeEmail = builder.assigneeEmail;
        this.project = builder.project;
        this.currentMonthYear = builder.currentMonthYear;
    }

    public static Builder builder() {
        return Builder.aNewCommentEntryDto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewCommentEntryDto that = (NewCommentEntryDto) o;
        return Objects.equals(getStepId(), that.getStepId()) && Objects.equals(getEmployeeEmail(), that.getEmployeeEmail()) && Objects.equals(getComment(), that.getComment()) && Objects.equals(getAssigneeEmail(), that.getAssigneeEmail()) && Objects.equals(getProject(), that.getProject()) && Objects.equals(getCurrentMonthYear(), that.getCurrentMonthYear());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStepId(), getEmployeeEmail(), getComment(), getAssigneeEmail(), getProject(), getCurrentMonthYear());
    }

    public Long getStepId() {
        return stepId;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public String getComment() {
        return comment;
    }

    public String getAssigneeEmail() {
        return assigneeEmail;
    }

    public String getProject() {
        return project;
    }

    public String getCurrentMonthYear() {
        return currentMonthYear;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private Long stepId;
        private String employeeEmail;
        private String comment;
        private String assigneeEmail;
        private String project;
        private String currentMonthYear;

        private Builder() {
        }

        public static Builder aNewCommentEntryDto() {
            return new Builder();
        }

        public Builder stepId(Long stepId) {
            this.stepId = stepId;
            return this;
        }

        public Builder employeeEmail(String employeeEmail) {
            this.employeeEmail = employeeEmail;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder assigneeEmail(String assigneeEmail) {
            this.assigneeEmail = assigneeEmail;
            return this;
        }

        public Builder project(String project) {
            this.project = project;
            return this;
        }

        public Builder currentMonthYear(String currentMonthYear) {
            this.currentMonthYear = currentMonthYear;
            return this;
        }

        public NewCommentEntryDto build() {
            return new NewCommentEntryDto(this);
        }
    }
}
