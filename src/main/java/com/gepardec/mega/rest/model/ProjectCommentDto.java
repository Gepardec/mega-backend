package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.LocalDate;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = ProjectCommentDto.Builder.class)
public class ProjectCommentDto {

    private final Long id;

    private final String comment;

    private final LocalDate date;

    private final String projectName;

    private ProjectCommentDto(Builder builder) {
        this.id = builder.id;
        this.comment = builder.comment;
        this.date = builder.date;
        this.projectName = builder.projectName;
    }

    public static Builder builder() {
        return Builder.aProjectCommentDto();
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

    public String getComment() {
        return comment;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getProjectName() {
        return projectName;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private Long id;
        private String comment;
        private LocalDate date;
        private String projectName;

        private Builder() {
        }

        public static Builder aProjectCommentDto() {
            return new Builder();
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public ProjectCommentDto build() {
            return new ProjectCommentDto(this);
        }
    }
}
