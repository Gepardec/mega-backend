package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.domain.model.ProjectState;
import com.gepardec.mega.domain.model.ProjectStep;

import java.util.Objects;

// TODO Discuss: Rename all rest pojos to DTO?
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectEntryDto {
    private final ProjectState state;

    private final boolean preset;

    private final String projectName;

    private final ProjectStep step;

    private final String currentMonthYear;

    @JsonCreator
    public ProjectEntryDto(Builder builder) {
        this.state = builder.state;
        this.preset = builder.preset;
        this.projectName = builder.projectName;
        this.step = builder.step;
        this.currentMonthYear = builder.currentMonthYear;
    }

    public static Builder builder() {
        return Builder.aProjectEntryDto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectEntryDto that = (ProjectEntryDto) o;
        return isPreset() == that.isPreset() && getState() == that.getState() && Objects.equals(getProjectName(), that.getProjectName()) && getStep() == that.getStep() && Objects.equals(getCurrentMonthYear(), that.getCurrentMonthYear());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getState(), isPreset(), getProjectName(), getStep(), getCurrentMonthYear());
    }

    public ProjectState getState() {
        return state;
    }

    public boolean isPreset() {
        return preset;
    }

    public String getProjectName() {
        return projectName;
    }

    public ProjectStep getStep() {
        return step;
    }

    public String getCurrentMonthYear() {
        return currentMonthYear;
    }

    public static final class Builder {
        @JsonProperty private ProjectState state;
        @JsonProperty private boolean preset;
        @JsonProperty private String projectName;
        @JsonProperty private ProjectStep step;
        @JsonProperty private String currentMonthYear;

        private Builder() {
        }

        public static Builder aProjectEntryDto() {
            return new Builder();
        }

        public Builder state(ProjectState state) {
            this.state = state;
            return this;
        }

        public Builder preset(boolean preset) {
            this.preset = preset;
            return this;
        }

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder step(ProjectStep step) {
            this.step = step;
            return this;
        }

        public Builder currentMonthYear(String currentMonthYear) {
            this.currentMonthYear = currentMonthYear;
            return this;
        }

        public ProjectEntryDto build() {
            return new ProjectEntryDto(this);
        }
    }
}
