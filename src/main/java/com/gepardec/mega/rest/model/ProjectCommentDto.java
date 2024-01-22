package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectCommentDto {

    private Long id;

    private String comment;

    private LocalDate date;

    private String projectName;

    public ProjectCommentDto() {
    }

    public ProjectCommentDto(Long id, String comment, LocalDate date, String projectName) {
        this.id = id;
        this.comment = comment;
        this.date = date;
        this.projectName = projectName;
    }

    public static ProjectCommentDtoBuilder builder() {
        return ProjectCommentDtoBuilder.aProjectCommentDto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectCommentDto that = (ProjectCommentDto) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getComment(), that.getComment()) && Objects.equals(getDate(), that.getDate()) && Objects.equals(getProjectName(), that.getProjectName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getComment(), getDate(), getProjectName());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public static final class ProjectCommentDtoBuilder {
        private Long id;
        private String comment;
        private LocalDate date;
        private String projectName;

        private ProjectCommentDtoBuilder() {
        }

        public static ProjectCommentDtoBuilder aProjectCommentDto() {
            return new ProjectCommentDtoBuilder();
        }

        public ProjectCommentDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProjectCommentDtoBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public ProjectCommentDtoBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public ProjectCommentDtoBuilder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public ProjectCommentDto build() {
            ProjectCommentDto projectCommentDto = new ProjectCommentDto();
            projectCommentDto.setId(id);
            projectCommentDto.setComment(comment);
            projectCommentDto.setDate(date);
            projectCommentDto.setProjectName(projectName);
            return projectCommentDto;
        }
    }
}
