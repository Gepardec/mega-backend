package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewCommentEntryDto {

    @JsonProperty
    private Long stepId;

    @JsonProperty
    private String employeeEmail;

    @JsonProperty
    private String comment;

    @JsonProperty
    private String assigneeEmail;

    @JsonProperty
    private String project;

    @JsonProperty
    private String currentMonthYear;

    public NewCommentEntryDto() {
    }

    public NewCommentEntryDto(Long stepId, String employeeEmail, String comment, String assigneeEmail, String project, String currentMonthYear) {
        this.stepId = stepId;
        this.employeeEmail = employeeEmail;
        this.comment = comment;
        this.assigneeEmail = assigneeEmail;
        this.project = project;
        this.currentMonthYear = currentMonthYear;
    }

    public static NewCommentEntryDtoBuilder builder() {
        return NewCommentEntryDtoBuilder.aNewCommentEntryDto();
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

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAssigneeEmail() {
        return assigneeEmail;
    }

    public void setAssigneeEmail(String assigneeEmail) {
        this.assigneeEmail = assigneeEmail;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getCurrentMonthYear() {
        return currentMonthYear;
    }

    public void setCurrentMonthYear(String currentMonthYear) {
        this.currentMonthYear = currentMonthYear;
    }

    public static final class NewCommentEntryDtoBuilder {
        private Long stepId;
        private String employeeEmail;
        private String comment;
        private String assigneeEmail;
        private String project;
        private String currentMonthYear;

        private NewCommentEntryDtoBuilder() {
        }

        public static NewCommentEntryDtoBuilder aNewCommentEntryDto() {
            return new NewCommentEntryDtoBuilder();
        }

        public NewCommentEntryDtoBuilder stepId(Long stepId) {
            this.stepId = stepId;
            return this;
        }

        public NewCommentEntryDtoBuilder employeeEmail(String employeeEmail) {
            this.employeeEmail = employeeEmail;
            return this;
        }

        public NewCommentEntryDtoBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public NewCommentEntryDtoBuilder assigneeEmail(String assigneeEmail) {
            this.assigneeEmail = assigneeEmail;
            return this;
        }

        public NewCommentEntryDtoBuilder project(String project) {
            this.project = project;
            return this;
        }

        public NewCommentEntryDtoBuilder currentMonthYear(String currentMonthYear) {
            this.currentMonthYear = currentMonthYear;
            return this;
        }

        public NewCommentEntryDto build() {
            NewCommentEntryDto newCommentEntryDto = new NewCommentEntryDto();
            newCommentEntryDto.setStepId(stepId);
            newCommentEntryDto.setEmployeeEmail(employeeEmail);
            newCommentEntryDto.setComment(comment);
            newCommentEntryDto.setAssigneeEmail(assigneeEmail);
            newCommentEntryDto.setProject(project);
            newCommentEntryDto.setCurrentMonthYear(currentMonthYear);
            return newCommentEntryDto;
        }
    }
}
